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
    println("%25s\t%25s\t%25s\t%25s".format(testConfig.id, allWallMsStr, allCpuMsStr, allAllocatedBytesStr))

  }

  // -XX:MaxInlineLevel=32
  //-XX:MaxInlineSize=35

  def runBenchmark(envConfig: EnvironmentConfig): Unit = {

    val commitsWithId = List(

      // 2.12.1 vs latest
//      TestConfig("00_baseline", BuildFromGit("2787b47396013a44072fa7321482103b66fbccd3")),
//      TestConfig("00_cache", BuildFromGit("5a5ed5826f297bca6291cd1b1effd3f7231215f9")),

//      TestConfig("00_baseline", BuildFromGit("875e5cf312ce3f1246367db822717067f94f97aa")),
//      TestConfig("00_cache", BuildFromGit("3411f80e39befd824b66636075aab6d6a86f8337"))

     // TestConfig("01_cache", BuildFromGit("5a5ed5826f297bca6291cd1b1effd3f7231215f9"),extraJVMArgs = List("-XX:MaxInlineLevel=18"))
//      TestConfig("01_highMIandIS", BuildFromGit("5a5ed5826f297bca6291cd1b1effd3f7231215f9"),extraJVMArgs = List("-XX:MaxInlineLevel=32","-XX:MaxInlineSize=70"))
//      TestConfig("00_linker_bl", BuildFromDir("S:/scala/scala_perf2", false)),
      TestConfig("00_linker", BuildFromDir("S:/scala/scala_perf2", false))
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

    if(envConfig.iterations > 10) {
      heading("after 10 90%")
      results.foreach { case (config, configResult) =>
        printAggResults(config, configResult.filterIteration(10, 10000).std)
      }

      heading("after 10 90% JVM, no GC")
      results.foreach { case (config, configResult) =>
        printAggResults(config, configResult.filterIteration(10, 10000).filterPhases("jvm").filterNoGc.std)
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
        %%("git", "cherry-pick", "e929236a4b419412fda44639bbe06313fc7c05bb")(dir) //profiler
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
