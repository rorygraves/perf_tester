package org.perftester

import java.io.File
import java.nio.file.{Files, Paths}

import ammonite.ops.{%%, Command, Path, Shellout, ShelloutException, read}
import org.perftester.process.Compiler._
import org.perftester.process.{IO, Parent, ProcessConfiguration}
import org.perftester.renderer.{HtmlRenderer, PhaseRenderer, TextRenderer}
import org.perftester.results.{PhaseResults, ResultReader, RunDetails, RunResult}
import org.perftester.sbtbot.SBTBotTestRunner
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.immutable.SortedSet
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ProfileMain {

  val log: Logger = LoggerFactory.getLogger("ProfileMain")

  def main(args: Array[String]): Unit = {
    // parser.parse returns Option[C]
    PerfTesterOptionParser.parser.parse(args, EnvironmentConfig()) match {
      case Some(envConfig) =>
        runBenchmark(envConfig)

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }

  val isWindows: Boolean = System.getProperty("os.name").startsWith("Windows")

  def printAggResults(cycleId: Int,
                      testConfig: TestConfig,
                      results: Seq[PhaseResults],
                      limit: Double): Unit =
    println(renderAggResults(cycleId, testConfig, results, limit))

  def aggResultsHeading(title: String) =
    f"-----\n$title\n${"Run Name"}%25s\tCycle\tsamples\t${"Wall time (ms)"}%25s\t${"All Wall time (ms)"}%25s\t${"CPU(ms)"}%25s\t${"Idle time (ms)"}%25s\t${"Allocated(MBs)"}%25s"

  def renderAggResults(cycleId: Int,
                       testConfig: TestConfig,
                       results: Seq[PhaseResults],
                       limit: Double): String = {

    val size = (results.size * limit).toInt
    case class Distribution(min: Double, max: Double, mean: Double) {
      def formatPercent(sigDigits: Int, decimalDigits: Int, value: Double): String = {
        String.format(s"%+$sigDigits.${decimalDigits}f", new java.lang.Double(value))
      }

      def formatResult(sigDigits: Int, decimalDigits: Int, value: Double): String = {
        String.format(s"%,$sigDigits.${decimalDigits}f", new java.lang.Double(value))
      }

      def formatted(s: Int, p: Int): String = {
        s"${formatResult(s, p, mean)} [${formatPercent(4, 2, (min / mean) * 100 - 100)}% ${formatPercent(4, 2, (max / mean) * 100 - 100)}%]"
      }
    }
    def distribution(fn: PhaseResults => Double): Distribution = {
      if (size < 1) Distribution(-1, -1, -1)
      else {
        val raw  = (results map fn sorted).take(size)
        val mean = raw.sum / size
        if (raw.isEmpty) Distribution(-1, -1, -1) else Distribution(raw.head, raw.last, mean)
      }
    }

    val wallClockTimeAvg    = distribution(_.wallClockTimeMS)
    val allWallClockTimeAvg = distribution(_.allWallClockTimeMS)
    val allCpuTimeAvg       = distribution(_.cpuTimeMS)
    val allAllocatedBytes   = distribution(_.allocatedMB)
    val allIdleAvg          = distribution(_.idleTimeMS)

    val wallMsStr            = wallClockTimeAvg.formatted(6, 2)
    val allWallMsStr         = allWallClockTimeAvg.formatted(6, 2)
    val allCpuMsStr          = allCpuTimeAvg.formatted(6, 2)
    val allAllocatedBytesStr = allAllocatedBytes.formatted(6, 2)
    val allIdleMsStr         = allIdleAvg.formatted(6, 2)

    "%25s\t%4s\t%4s\t%25s\t%25s\t%25s\t%25s\t%25s"
      .format(testConfig.id,
              cycleId,
              size,
              wallMsStr,
              allWallMsStr,
              allCpuMsStr,
              allIdleMsStr,
              allAllocatedBytesStr)

  }

  // -XX:MaxInlineLevel=32
  //-XX:MaxInlineSize=35

  def parseConfigString(configString: String): List[TestConfig] = {
    configString.split("\\|").toList.map { configLine =>
      println("configLine = " + configLine)
      val id :: gitHash :: extraOptionsString = configLine.split(";").toList
      val extraOptions                        = extraOptionsString.flatMap(_.split(",").toList)
      TestConfig(id, BuildFromGit(gitHash), extraArgs = extraOptions)
    }
  }

  def runBenchmark(envConfig: EnvironmentConfig): Unit = {

    val commitsWithId = Configurations.configurations
      .get(envConfig.config)
      .orElse(Option(envConfig.configString).map(parseConfigString))
      .getOrElse {
        println(s"[ERROR] Config ${envConfig.config} not found")
        throw new IllegalArgumentException(s"Config ${envConfig.config} not found")
      }

    val outputFolder = envConfig.outputDir / envConfig.username / envConfig.config
    Files.createDirectories(outputFolder.toNIO)
    println("Output logging to " + outputFolder)

    val results: SortedSet[RunDetails] = {
      var all = SortedSet.empty[RunDetails]
      for (vmId                 <- 1 to envConfig.processes;
           (testConfig, testId) <- commitsWithId.zipWithIndex) {

        val plan = planRun(envConfig, outputFolder, testConfig, vmId, envConfig.iterations)
        if (plan.runTest && all.nonEmpty)
          TextRenderer.outputTextResults(envConfig, all)
        val results = executeRuns(plan)

        all += RunDetails(vmId, testId, results)
      }
      all
    }

    TextRenderer.outputTextResults(envConfig, results)
    HtmlRenderer.outputHtmlResults(outputFolder, envConfig, results)

    println("\n\n")
    println(PhaseRenderer.outputHtmlResults(outputFolder, envConfig, results))
  }

  def planRun(
      envConfig: EnvironmentConfig,
      outputFolder: Path,
      testConfig: TestConfig,
      vm: Int,
      repeat: Int
  ): RunPlan = {
    val (sourceDir: Path, reuseScalac: Boolean, scalacPackDir: Path) = testConfig match {
      case TestConfig(_, BuildFromGit(sha, customDir), _, _, _) =>
        val targetDir = customDir.getOrElse(envConfig.checkoutDir)
        val packDir   = envConfig.scalacBuildCache / sha
        val reused    = Files.exists(packDir / flag toNIO)
        (targetDir, reused, packDir)
      case TestConfig(_, bfd @ BuildFromDir(_, _, rebuild), _, _, _) =>
        val sourceDir   = bfd.path
        val targetBuild = buildDir(sourceDir)
        val reuse = {
          if (!Files.exists(targetBuild.toNIO)) {
            println(s"dir NOT reused - no build dir")
            false
          } else if (rebuild) {
            println(s"dir NOT reused - as rebuild requested")
            false
          } else if (!Files.exists(targetBuild / flag toNIO)) {
            println(s"dir NOT reused - no flag file")
            false
          } else {
            val sourceDT = Utils.lastChangedDate(sourceDir / "src")
            val buildDT  = Utils.lastChangedDate(targetBuild)
            println(s"latest file times \nsource $sourceDT\nbuild  $buildDT")
            val reuse = sourceDT._1.isBefore(buildDT._1)
            println(s"dir reused = $reuse - based on file times")
            reuse
          }
        }

        (sourceDir, reuse, targetBuild)
    }

    val profileOutputFile = outputFolder / s"run_${vm}_${testConfig.id}.csv"

    val exists = Files.exists(profileOutputFile.toNIO)

    val runTest   = !envConfig.analyseOnly && (!exists || envConfig.overwriteResults || testConfig.buildDefn.forceOverwriteResults)
    val runScalac = !envConfig.analyseOnly && runTest && !reuseScalac
    RunPlan(runTest,
            vm,
            reuseScalac,
            sourceDir,
            scalacPackDir,
            profileOutputFile,
            runScalac,
            testConfig,
            repeat,
            envConfig)
  }

  case class RunPlan(runTest: Boolean,
                     vm: Int,
                     canReuseScalac: Boolean,
                     scalaSourceDir: Path,
                     scalaPackDir: Path,
                     profileOutputFile: Path,
                     runScalac: Boolean,
                     testConfig: TestConfig,
                     repeats: Int,
                     envConfig: EnvironmentConfig)

  def executeRuns(
      runPlan: RunPlan
  ): RunResult = {
    val action = {
      if (runPlan.runTest && runPlan.runScalac) "compile and test"
      else if (runPlan.runTest) "test"
      else "skip"
    }
    println(
      "\n\n******************************************************************************************************")
    println(
      s"EXECUTING RUN #${runPlan.vm} ${runPlan.testConfig.id} - ${runPlan.testConfig.buildDefn}      $action")
    println(
      "******************************************************************************************************\n\n")

    if (runPlan.runTest) {
      if (runPlan.runScalac)
        buildScalaC(runPlan.testConfig.buildDefn, runPlan.scalaSourceDir, runPlan.scalaPackDir)
      //flag the build was used
      Utils.touch(runPlan.scalaPackDir toNIO)
      executeTest(runPlan)
    }

    ResultReader.readResults(runPlan.testConfig, runPlan.profileOutputFile, runPlan.repeats)
  }

  def buildScalaC(buildDefn: BuildType, sourceDir: Path, scalaPackDir: Path): Unit = {
    buildDefn match {
      case BuildFromGit(hash, _) =>
        try {
          log.info(s"Running: git fetch    (in $sourceDir)")
          %%("git", "fetch")(sourceDir).out.lines.foreach(log.info)
          log.info(s"Running: git reset --hard $hash    (in $sourceDir)")
          %%("git", "reset", "--hard", hash)(sourceDir).out.lines.foreach(log.info)
        } catch {
          case t: ShelloutException =>
            if (t.result.err.string.contains("fatal: Could not parse object") ||
                t.result.out.string.contains("fatal: Could not parse object"))
              log.error(s"Failed to fetch and build hash $hash - '" + " cannot resolve hash")
            log.error(s"Failed to execute git fetch/reset to $hash", t)
            System.exit(1)
        }
      case bfd: BuildFromDir =>
        log.info("BuildFromDir selected - fetch skipped")
    }

    log.info(s"Building compiler in $sourceDir")

    runSbt(List("setupPublishCore", "clean", "dist/mkPack"), sourceDir, Nil)
    if (scalaPackDir != buildDir(sourceDir)) {
      val nioScalaPackDir = scalaPackDir.toNIO
      Utils.deleteDir(nioScalaPackDir)
//      mkdir(scalaPackDir)
      Utils.copy(buildDir(sourceDir) toNIO, nioScalaPackDir)
    }
    Utils.touch(scalaPackDir / flag toNIO)
  }

  //flag file to indicate that the mkPack completed successfully
  val flag = "mkPack.success"

  def buildDir(path: Path): Path = path / "build" / "pack"

  def executeTest(runPlan: RunPlan): Unit = {
    val mkPackPath = runPlan.scalaPackDir
    val debugArgs =
      if (runPlan.envConfig.runWithDebug)
        "-agentlib:jdwp=transport=dt_shmem,server=y,suspend=y" :: Nil
      else Nil

    val profileParams = List("-Yprofile-destination", runPlan.profileOutputFile.toString())

    log.info("Logging stats to " + runPlan.profileOutputFile)
    if (Files.exists(runPlan.profileOutputFile.toNIO))
      Files.delete(runPlan.profileOutputFile.toNIO)

    val jvmArgs = debugArgs ::: runPlan.testConfig.extraJVMArgs
    if (runPlan.testConfig.useSbt) {
      val extraArgsStr =
        if (runPlan.testConfig.extraArgs.nonEmpty)
          runPlan.testConfig.extraArgs.mkString("\"", "\",\"", "\", ")
        else ""

      val programArgs = List(
        s"++2.13.0-M3=$mkPackPath",
        s"""set scalacOptions in ThisBuild ++= List($extraArgsStr${profileParams
          .mkString("\"", "\",\"", "\"")})"""
      )

      val dotfile = runPlan.envConfig.testDir / ".perf_tester"
      val sbtCommands =
        if (dotfile.toIO.exists()) read.lines(dotfile).toList.filterNot(_.trim.isEmpty)
        else "clean" :: "compile" :: Nil // slightly bogus default

      SBTBotTestRunner.run(runPlan.envConfig.testDir,
                           programArgs,
                           jvmArgs,
                           runPlan.repeats,
                           sbtCommands,
                           runPlan.envConfig.runWithDebug)
    } else {
      val mkPackLibPath = (mkPackPath / "lib").toString()
      val classPath = List(
        "jline.jar",
        "scala-compiler-doc.jar",
        "scala-compiler.jar",
        "scala-library.jar",
        "scala-reflect.jar",
        "scala-repl-jline-embedded.jar",
        "scala-repl-jline.jar",
        "scala-swing_2.12-2.0.0.jar",
        "scala-xml_2.12-1.0.6.jar",
        "scalap.jar"
      ).map(mkPackPath + File.separator + "lib" + File.separator + _)

      //      s"${lib}jline.jar;${lib}scala-compiler-doc.jar;${lib}scala-compiler.jar;${lib}scala-library.jar;${lib}scala-reflect.jar;${lib}scala-repl-jline-embedded.jar;${lib}scala-repl-jline.jar;${lib}scala-swing_2.12-2.0.0.jar;${lib}scala-xml_2.12-1.0.6.jar;${lib}scalap.jar"
      val params = List(
        "-XX:MaxInlineLevel=32",
        //"-XX:MaxInlineSize=35",
        "-Xmx10G",
        "-Xms32M",
        s"""-Dscala.home="$mkPackPath${File.separator}.."""",
        """-Denv.emacs="" """,
        "-Dscala.usejavacp=true"
      ) ::: debugArgs ::: runPlan.testConfig.extraJVMArgs

      //TODO need to read this
      val files = IO.listSourcesIn(Paths.get("S:/scala/akka/akka-actor/src/main/scala")) map (_.toString)

      //TODO need to read this
      val compileClassPath = List(
        "C:\\Users\\dev\\.ivy2\\cache\\org.scala-lang.modules\\scala-java8-compat_2.12\\bundles\\scala-java8-compat_2.12-0.8.0.jar",
        "C:\\Users\\dev\\.ivy2\\cache\\com.typesafe\\config\\bundles\\config-1.3.1.jar",
        "C:\\Users\\dev\\.m2\\repository\\com\\typesafe\\akka\\akka-actor_2.12\\2.5.1\\akka-actor_2.12-2.5.1.jar"
      )

      //TODO need to read this
      val otherParams =
        List(
          "-sourcepath",
          s"${runPlan.envConfig.testDir}${File.separator}src${File.separator}main${File.separator}java${File.pathSeparator}${runPlan.envConfig.testDir}${File.separator}src${File.separator}main${File.separator}scala"
        ) ++ profileParams

      val id         = "x"
      val parent     = new Parent(ProcessConfiguration(new File("."), None, classPath, params))
      val outputPath = "z:\\output\\"
      val outputDir  = Path(outputPath)
      Files.createDirectories(outputDir.toNIO)

      parent.createGlobal(id, outputPath, compileClassPath, otherParams, files)
      for (cycle <- 1 to runPlan.envConfig.iterations) {
        val result = parent.runGlobal(id)
        println(s" run ${runPlan.vm} # $cycle took ${result / 1000 / 1000.0} ms")

        val delete = startClean(outputDir)

        parent.doGc()

        Await.result(delete, Duration.Inf)
      }
      parent.destroyGlobal(id)
      parent.doExit()
    }
  }
  def startClean(file: Path) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Future

    Future {
      IO.deleteDir(file.toNIO, false)
    }
  }

  def sbtCommandLine(extraJVMArgs: List[String]): List[String] = {
    val sbt = new File("sbtlib/sbt-launch.jar").getAbsoluteFile
    require(sbt.exists(), "sbt-launch.jar must exist in sbtlib directory")
    val sbtOpts = sys.env.get("SBT_OPTS").toList.flatMap(_.split(" ")).filterNot(_.trim.isEmpty)
    List("java",
         "-Dfile.encoding=UTF8",
         "-Xmx12G",
         "-XX:MaxPermSize=256m",
         "-XX:ReservedCodeCacheSize=128m",
         "-Dsbt.log.format=true",
         "-mx2G") ::: extraJVMArgs ::: sbtOpts ::: List("-cp", sbt.toString, "xsbt.boot.Boot")
  }

  def runSbt(command: List[String], dir: Path, extraJVMArgs: List[String]): Unit = {
    import collection.JavaConverters._

    val escaped = if (isWindows) command map { s =>
      s.replace("\\", "\\\\").replace("\"", "\\\"")
    } else command

    val fullCommand = sbtCommandLine(extraJVMArgs) ::: escaped
    log.info(s"running sbt : ${fullCommand.mkString("'", "' '", "'")}")
    val proc = new ProcessBuilder(fullCommand.asJava)
    proc.directory(dir.toIO)
    proc.inheritIO()
    proc.start().waitFor() match {
      case 0 =>
      case r => throw new IllegalStateException(s"bad result $r")
    }
  }
}
