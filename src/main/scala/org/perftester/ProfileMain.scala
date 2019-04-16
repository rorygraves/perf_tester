package org.perftester

import java.io.File
import java.nio.file.{Files, Paths}

import ammonite.ops.{Command, Path, Shellout, read}
import org.perftester.git.GitUtils
import org.perftester.process.{IO, Parent, ProcessConfiguration}
import org.perftester.renderer.{HtmlRenderer, TextRenderer}
import org.perftester.results.{PhaseResults, ResultReader, RunDetails, RunResult}
import org.perftester.sbtbot.SBTBotTestRunner
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.immutable.SortedSet
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ProfileMain {

  val log: Logger        = LoggerFactory.getLogger("ProfileMain")
  val isWindows: Boolean = System.getProperty("os.name").startsWith("Windows")
  //flag file to indicate that the mkPack completed successfully
  val flag = "mkPack.success"

  def main(args: Array[String]): Unit = {
    // parser.parse returns Option[C]
    PerfTesterOptionParser.parser.parse(args, EnvironmentConfig()) match {
      case Some(envConfig) =>
        runBenchmark(envConfig)

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }

  def runBenchmark(envConfig: EnvironmentConfig): Unit = {

    //for some reason the jgit command doesnt work
    //so use the shell
    log.info(s"Running: git fetch    (in ${envConfig.checkoutDir})")
    Command(Vector.empty, sys.env, Shellout.executeStream)("git", "fetch", "--all")(
      envConfig.checkoutDir)

    //jgit API that doesnt work please fix
    //    val git = GitUtils(envConfig.checkoutDir)
    //    try {
    //      git.fetchAll()
    //    } finally git.dispose()

    val commitsWithId = Configurations
      .configurationsFor(envConfig)
      .get(envConfig.config)
      .orElse(Option(envConfig.configString).map(configString =>
        (() => parseConfigString(configString))))
      .getOrElse {
        println(s"[ERROR] Config ${envConfig.config} not found")
        throw new IllegalArgumentException(s"Config ${envConfig.config} not found")
      }()

    val outputFolder = envConfig.outputDir / envConfig.username / envConfig.config
    Files.createDirectories(outputFolder.toNIO)
    println("Output logging to " + outputFolder)

    val results: SortedSet[RunDetails] = {
      var all = SortedSet.empty[RunDetails]
      for (vmId                 <- 1 to envConfig.processes;
           (testConfig, testId) <- commitsWithId.zipWithIndex) {

        val plan = planRun(envConfig, outputFolder, testConfig, vmId, envConfig.iterations)
        if (plan.runTest && all.nonEmpty)
          TextRenderer(envConfig, all).outputTextResults
        val results = executeRuns(plan)

        all += RunDetails(vmId, testId, results)
      }
      all
    }

    TextRenderer(envConfig, results).outputTextResults
    HtmlRenderer.outputHtmlResults(outputFolder, envConfig, results)
  }

  def parseConfigString(configString: String): List[TestConfig] = {
    configString.split("\\|").toList.map { configLine =>
      println("configLine = " + configLine)
      val id :: gitHash :: extraOptionsString = configLine.split(";").toList
      val extraOptions                        = extraOptionsString.flatMap(_.split(",").toList)
      TestConfig(id, BuildFromGit(gitHash), extraArgs = extraOptions)
    }
  }

  // -XX:MaxInlineLevel=32
  //-XX:MaxInlineSize=35

  def planRun(
      envConfig: EnvironmentConfig,
      outputFolder: Path,
      testConfig: TestConfig,
      vm: Int,
      repeat: Int
  ): RunPlan = {
    val (sourceDir: Path, reuseScalac: Boolean, scalacPackDir: Path) = testConfig match {
      case TestConfig(_, gitBuild @ BuildFromGit(baseSha, cherryPicks, customDir), _, _, _) =>
        val targetDir = customDir.getOrElse(envConfig.checkoutDir)
        val packDir   = envConfig.scalacBuildCache / gitBuild.fullShaName
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

  def buildDir(path: Path): Path = path / "build" / "pack"

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
      case BuildFromGit(baseSha, cherryPicks, _) =>
        val git = GitUtils(sourceDir)
        try {
          //we always fetch once for the whole run
          log.info(s"Running: git reset --hard $baseSha    (in $sourceDir)")
          git.resetToRevision(baseSha)
          cherryPicks foreach { sha =>
            log.info(s"Running: git cherry-pick $sha    (in $sourceDir)")
            git.cherryPick(sha)
          }
        } catch {
          case t: Exception =>
            log.error(s"Failed to execute git fetch/reset to $baseSha ${if (cherryPicks isEmpty) ""
            else "or cherry-pick to " + cherryPicks.mkString}", t)
            throw t
        } finally {
          git.dispose()
        }
      case bfd: BuildFromDir =>
        log.info("BuildFromDir selected - fetch skipped")
    }

    log.info(s"Building compiler in $sourceDir")

    runSbt(List("setupPublishCore", "clean", "dist/mkPack"), sourceDir, Nil)
    if (scalaPackDir != buildDir(sourceDir)) {
      val nioScalaPackDir = scalaPackDir.toNIO
      Files.createDirectories(nioScalaPackDir)
      Utils.deleteDir(nioScalaPackDir)
      Utils.copy(buildDir(sourceDir) toNIO, nioScalaPackDir)
    }
    Utils.touch(scalaPackDir / flag toNIO)
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
        s"""set scalaHome := Some(file("$mkPackPath"))""",
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
        ) ++ profileParams ++ runPlan.testConfig.extraArgs

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

  def printAggResults(cycleId: Int,
                      testConfig: TestConfig,
                      summary: Boolean,
                      stats: Stats,
                      baselineStats: Stats,
                      baselineId: String) = {
    val wallMsStr            = stats.wallClockTimeAvg.formatted(6, 2)
    val allWallMsStr         = stats.allWallClockTimeAvg.formatted(6, 2)
    val allCpuMsStr          = stats.allCpuTimeAvg.formatted(6, 2)
    val allAllocatedBytesStr = stats.allAllocatedBytes.formatted(6, 2)
    val allIdleMsStr         = stats.allIdleAvg.formatted(6, 2)

    def diff(thisTest: Distribution, baseline: Distribution) = {
      if (baseline.mean == 0) "N/A"
      else if (thisTest.mean == baseline.mean) "---"
      else if (thisTest.mean > baseline.mean)
        f"+${(thisTest.mean - baseline.mean) / baseline.mean * 100}%6.2f%%"
      else f"${(thisTest.mean - baseline.mean) / baseline.mean * 100}%6.2f%%"
    }
    if (summary) {
      println(
        "%-65s\t%4s\t%25s\t%25s\t%25s\t%25s\t%25s"
          .format(testConfig.id,
                  stats.size,
                  wallMsStr,
                  allWallMsStr,
                  allCpuMsStr,
                  allIdleMsStr,
                  allAllocatedBytesStr))
      if (testConfig.id != baselineId)
        println(
          "%-65s\t%4s\t%15s\t%25s\t%25s\t%25s\t%25s"
            .format(
              "... v baseline",
              "",
              diff(stats.wallClockTimeAvg, baselineStats.wallClockTimeAvg),
              diff(stats.allWallClockTimeAvg, baselineStats.allWallClockTimeAvg),
              diff(stats.allCpuTimeAvg, baselineStats.allCpuTimeAvg),
              diff(stats.allIdleAvg, baselineStats.allIdleAvg),
              diff(stats.allAllocatedBytes, baselineStats.allAllocatedBytes)
            ))
    } else
      println(
        "%-65s\t%4s\t%4s\t%25s\t%25s\t%25s\t%25s\t%25s"
          .format(testConfig.id,
                  cycleId,
                  stats.size,
                  wallMsStr,
                  allWallMsStr,
                  allCpuMsStr,
                  allIdleMsStr,
                  allAllocatedBytesStr))
  }
  def calcStats(results: Seq[PhaseResults], limit: Double): Stats = {

    val size = (results.size * limit).toInt
    def distribution(fn: PhaseResults => Double): Distribution = {
      if (results.isEmpty) Distribution(-1, -1, -1)
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
    Stats(size, wallClockTimeAvg, allWallClockTimeAvg, allCpuTimeAvg, allAllocatedBytes, allIdleAvg)
  }

  case class Distribution(min: Double, max: Double, mean: Double) {
    def formatted(s: Int, p: Int): String = {
      s"${formatResult(s, p, mean)} [${formatPercent(4, 2, (min / mean) * 100 - 100)}% ${formatPercent(4, 2, (max / mean) * 100 - 100)}%]"
    }

    def formatPercent(sigDigits: Int, decimalDigits: Int, value: Double): String = {
      String.format(s"%+$sigDigits.${decimalDigits}f", new java.lang.Double(value))
    }

    def formatResult(sigDigits: Int, decimalDigits: Int, value: Double): String = {
      String.format(s"%,$sigDigits.${decimalDigits}f", new java.lang.Double(value))
    }
  }

  case class Stats(size: Int,
                   wallClockTimeAvg: Distribution,
                   allWallClockTimeAvg: Distribution,
                   allCpuTimeAvg: Distribution,
                   allAllocatedBytes: Distribution,
                   allIdleAvg: Distribution)

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
}
