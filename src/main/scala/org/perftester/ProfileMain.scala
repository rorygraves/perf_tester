package org.perftester

import java.io.File
import java.nio.file.Files

import ammonite.ops.{%%, Path}
import org.perftester.results.{ResultReader, RunResult}

import scala.collection.mutable


object ProfileMain {

  def main(args: Array[String]): Unit = {
    // parser.parse returns Option[C]
    PerfTesterParser.parser.parse(args, EnvironmentConfig()) match {
      case Some(envConfig) =>
        runBenchmark(envConfig)

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }

  val isWindows: Boolean = System.getProperty("os.name").startsWith("Windows")

  def printAggResults(testConfig: TestConfig, results: RunResult#Detail): Unit = {
    val allWallClockTimeAvg = results.allWallClockMS
    val allCpuTimeAvg = results.allCPUTime
    val allAllocatedBytes = results.allAllocated
    val allWallMsStr = allWallClockTimeAvg.formatted(6,2)
    val allCpuMsStr = allCpuTimeAvg.formatted(6,2)
    val allAllocatedBytesStr = allAllocatedBytes.formatted(6,2)
    val size = results.size.toInt
    println("%25s\t%4s\t%25s\t%25s\t%25s".format(testConfig.id, size, allWallMsStr, allCpuMsStr, allAllocatedBytesStr))

  }

  // -XX:MaxInlineLevel=32
  //-XX:MaxInlineSize=35

  def runBenchmark(envConfig: EnvironmentConfig): Unit = {

    val commitsWithId = List(

//      TestConfig("00_2.12.2", BuildFromGit("21d12e9f5ec1ffe023f509848911476c1552d06f"),extraJVMArgs = List()),
//      TestConfig("00_2.12.x", BuildFromGit("e1e8d050deb643ca56db1549e2e5a3114572a952"),extraJVMArgs = List())

//      TestConfig("00_backend-baseline", BuildFromDir("S:/scala/backend-before", false), extraArgs = List("-Yprofile-external-tool", "jvm")),
      TestConfig("00_backend-baseline", BuildFromDir("S:/scala/backend-before", false), extraArgs = List("-Yprofile-run-gc", "*")),

//      TestConfig("00_backend-0-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "0")),
//      TestConfig("00_backend-2-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "2")),
//      TestConfig("00_backend-4-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4")),
//      TestConfig("00_backend-8-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8")),
//      TestConfig("00_backend-12-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "12")),
//      TestConfig("00_backend-16-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "16"))

      TestConfig("00_backend-0", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "0", "-Yprofile-run-gc", "all")),
      TestConfig("00_backend-1", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-Yprofile-run-gc", "all")),
      TestConfig("00_backend-2", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-Yprofile-run-gc", "all")),
      TestConfig("00_backend-3", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-Yprofile-run-gc", "all")),
      TestConfig("00_backend-4", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-Yprofile-run-gc", "all"))
//      TestConfig("00_backend-4-6", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-YmaxQueue", "6")),
//      TestConfig("00_backend-4-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-YmaxQueue", "8")),
//      TestConfig("00_backend-4-12", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-YmaxQueue", "12")),
//      TestConfig("00_backend-4-16", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-YmaxQueue", "16")),
//      TestConfig("00_backend-5", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "5", "-Yprofile-run-gc", "all")),
//      TestConfig("00_backend-6", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-Yprofile-run-gc", "all")),
//      TestConfig("00_backend-6-6", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-YmaxQueue", "6")),
//      TestConfig("00_backend-6-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-YmaxQueue", "8")),
//      TestConfig("00_backend-6-12", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-YmaxQueue", "12")),
//      TestConfig("00_backend-6-16", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-YmaxQueue", "16")),
//      TestConfig("00_backend-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-Yprofile-run-gc", "all"))
//      TestConfig("00_backend-8-12", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-YmaxQueue", "12")),
//      TestConfig("00_backend-8-16", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-YmaxQueue", "16")),
//      TestConfig("00_backend-8-20", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-YmaxQueue", "20")),
//      TestConfig("00_backend-8-24", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-YmaxQueue", "24")),
//      TestConfig("00_backend-10", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "10")),
//      TestConfig("00_backend-12", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "12")),
//      TestConfig("00_backend-14", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "14")),
//      TestConfig("00_backend-16", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "16"))


//      TestConfig("00_backend-3-A", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "16")),

//      TestConfig("00_backend-0", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "0")),
//      TestConfig("00_backend-1-4", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-YmaxQueue", "4")),
//      TestConfig("00_backend-1-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-YmaxQueue", "8")),
//      TestConfig("00_backend-1-X", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-YmaxQueue", "12")),
//      TestConfig("00_backend-2-4", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-YmaxQueue", "4")),
//      TestConfig("00_backend-2-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-YmaxQueue", "8")),
//      TestConfig("00_backend-2-X", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-YmaxQueue", "12")),
//      TestConfig("00_backend-3-4", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "4")),
//      TestConfig("00_backend-3-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "8")),
//      TestConfig("00_backend-3-X", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "12")),
//      TestConfig("00_backend-3-A", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "16"))

    )

    val results = commitsWithId map { testConfig =>
      val results = executeRuns(envConfig, testConfig, envConfig.iterations)
      (testConfig, results)
    }

    def heading(title: String) {
      println(f"$title\n\n${"RunName"}%25s\t${"AllWallMS"}%25s\t${"CPU_MS"}%25s\t${"Allocated"}%25s")
    }

    heading("ALL")
    results.foreach { case (config, configResult) =>
      printAggResults(config, configResult.all)
    }

    if (envConfig.iterations > 10) {
      heading("after 10 90%")
      results.foreach { case (config, configResult) =>
        printAggResults(config, configResult.filterIteration(10, 10000).std)
      }

      val phases: mutable.LinkedHashSet[String] = results.flatMap(r => r._2.phases)(scala.collection.breakOut)

      for (phase <- phases) {
        heading(s"after 10 90%, phase $phase, no GC")
        for {(config, configResult) <- results} {
          printAggResults(config, configResult.filterIteration(10, 10000).filterPhases(phase).filterNoGc.std)
        }
      }
    }
    if (envConfig.iterations > 20) {
      heading("after 20 90%")
      results.foreach { case (config, configResult) =>
        printAggResults(config, configResult.filterIteration(20, 10000).std)
      }

      val phases: mutable.LinkedHashSet[String] = results.flatMap(r => r._2.phases)(scala.collection.breakOut)
      for (phase <- phases) {
        heading(s"after 20 90%, phase $phase, no GC")
        for {(config, configResult) <- results} {
          printAggResults(config, configResult.filterIteration(20, 10000).filterPhases(phase).filterNoGc.std)
        }
      }
    }

  }

  private val lastBuiltScalac = mutable.Map[Path, String]()

  def executeRuns(envConfig: EnvironmentConfig, testConfig: TestConfig, repeat: Int): RunResult = {
    val (dir: Path, reused) = testConfig match {
      case TestConfig(_, BuildFromGit(sha, customDir), _, _) =>
        val targetDir = customDir.getOrElse(envConfig.checkoutDir)
        val reused = lastBuiltScalac.get(targetDir).contains(sha)
        lastBuiltScalac(targetDir) = sha
        (targetDir, reused)
      case TestConfig(_, BuildFromDir(sourceDir, _, rebuild), _, _) =>
        val reuse = {
          if (lastBuiltScalac.contains(sourceDir)) {
            println (s"dir reused - already used")
            true
          } else {
            val targetBuild = buildDir(sourceDir)
            if (!Files.exists(targetBuild.toNIO)) {
              println (s"dir NOT reused - no build dir")
              false
            } else if (rebuild) {
              println (s"dir NOT reused - as rebuild requested")
              false
            } else {
              val sourceDT = Utils.lastChangedDate(sourceDir / "src")
              val buildDT = Utils.lastChangedDate(targetBuild)
              println (s"latest file times \nsource $sourceDT\nbuild  $buildDT")
              val reuse = sourceDT._1.isBefore(buildDT._1)
                println (s"dir reused = $reuse - based on file times")
              reuse
            }
          }
        }

        (sourceDir, reuse)
    }
    val profileOutputFile = envConfig.outputDir / s"run_${testConfig.id}.csv"

    val exists = Files.exists(profileOutputFile.toNIO)

    val runTest = !envConfig.analyseOnly && (!exists || envConfig.overwriteResults || testConfig.buildDefn.forceOverwriteResults)
    val runScalac = !envConfig.analyseOnly && runTest && !reused
    val action = {
      if (runTest && runScalac) "compile and test"
      else if (runTest) "test"
      else "skip"
    }

    println("\n\n******************************************************************************************************")
    println(s"EXECUTING RUN ${testConfig.id} - ${testConfig.buildDefn}      $action")
    println("******************************************************************************************************\n\n")

    if (runTest) {
      if (!reused)
        buildScalaC(testConfig.buildDefn, dir)
      executeTest(envConfig, testConfig, profileOutputFile, repeat)
    }
    ResultReader.readResults(testConfig, profileOutputFile, repeat)
  }

  def buildScalaC(buildDefn:BuildType, dir: Path): Unit = {
    buildDefn match {
      case BuildFromGit(hash, _) =>
        %%("git", "fetch")(dir)
        %%("git", "reset", "--hard", hash)(dir)
      case _ =>
    }
    runSbt(List("""set scalacOptions in Compile in ThisBuild += "optimise" """, "dist/mkPack"), dir, Nil)
  }
  def buildDir(path :Path) = path  / "build" / "pack"

  def executeTest(envConfig: EnvironmentConfig, testConfig: TestConfig, profileOutputFile:Path, repeats: Int): Unit = {
    val mkPackPath = testConfig.buildDefn match {
      case BuildFromDir(dir,_, _) => buildDir(dir)
      case BuildFromGit(_,_) => buildDir(envConfig.checkoutDir)
    }
    println("Logging stats to " + profileOutputFile)
    if (Files.exists(profileOutputFile.toNIO))
      Files.delete(profileOutputFile.toNIO)
    val extraArgsStr = if (testConfig.extraArgs.nonEmpty) testConfig.extraArgs.mkString("\"", "\",\"", "\",") else ""

    val args = List(s"++2.12.1=$mkPackPath", //"-debug",
      s"""set scalacOptions in Compile in ThisBuild ++=List($extraArgsStr"-Yprofile-destination","$profileOutputFile")""") ++
      List.fill(repeats)(List("clean", "akka-actor/compile")).flatten


    val debugArgs=if (envConfig.runWithDebug)
      "-agentlib:jdwp=transport=dt_shmem,server=y,suspend=y" :: Nil else Nil

    runSbt(args, envConfig.testDir, debugArgs ::: testConfig.extraJVMArgs)
  }


  def sbtCommandLine(extraJVMArgs: List[String]): List[String] = {
    val sbt = new File("sbtlib/sbt-launch.jar").getAbsoluteFile
    require(sbt.exists(),"sbt-launch.jar must exist in sbtlib directory")
    List("java", "-Xmx12G", "-XX:MaxPermSize=256m", "-XX:ReservedCodeCacheSize=128m", "-Dsbt.log.format=true", "-mx12G") ::: extraJVMArgs ::: List("-cp", sbt.toString, "xsbt.boot.Boot")
  }

  def runSbt(command: List[String], dir: Path, extraJVMArgs: List[String]): Unit = {
    import collection.JavaConverters._

    val escaped = if (isWindows) command map {
      s => s.replace("\\", "\\\\").replace("\"", "\\\"")
    } else command

    val fullCommand = sbtCommandLine(extraJVMArgs) ::: escaped
    println(s"running sbt : ${fullCommand.mkString("'", "' '", "'")}")
    val proc = new ProcessBuilder(fullCommand.asJava)
    proc.directory(dir.toIO)
    proc.inheritIO()
    proc.start().waitFor() match {
      case 0 =>
      case r => throw new IllegalStateException(s"bad result $r")
    }
  }

  def getRevisions(base: String, checkoutDir: Path): List[String] = {
    val res = %%("git", "rev-list", "--no-merges", s"$base..HEAD")(checkoutDir)
    val commits = res.out.lines.toList.reverse
    commits
  }

}
