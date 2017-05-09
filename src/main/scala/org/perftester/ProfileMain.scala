package org.perftester

import java.io.File
import java.nio.file.Files

import ammonite.ops.{%%, Path, _}
import org.perftester.results.{ResultReader, RunResult}

import scala.collection.mutable


object ProfileMain {

  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      println("Usage: ProfileMain <checkoutDir> <testDir> <outputDir>")
      System.exit(1)
    }
    val checkoutDir = Path(new File(args(0)).getAbsolutePath)
    val testDir = Path(new File(args(1)).getAbsolutePath)
    val outputDir = Path(new File(args(2)).getAbsolutePath)
    mkdir! outputDir
    val envConfig = EnvironmentConfig(checkoutDir, testDir, outputDir, iterations = 1)
    runBenchmark(envConfig)
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
      TestConfig("00_baseline", BuildFromGit("5a5ed5826f297bca6291cd1b1effd3f7231215f9"),extraJVMArgs = List()),
      TestConfig("01_highMI", BuildFromGit("5a5ed5826f297bca6291cd1b1effd3f7231215f9"),extraJVMArgs = List("-XX:MaxInlineLevel=32")),
      TestConfig("01_highMIandIS", BuildFromGit("5a5ed5826f297bca6291cd1b1effd3f7231215f9"),extraJVMArgs = List("-XX:MaxInlineLevel=32","-XX:MaxInlineSize=70"))
    )

    val results = commitsWithId map { testConfig =>
      val results = executeRuns(envConfig, testConfig, envConfig.iterations)
      (testConfig, results)
    }

    def heading(title: String) {
//      println(f"$title\n\n${"RunName"}%25s\t${"AllWallMS"}%25s\t${"JVMWallMS"}%25s\t${"JVMUserMS"}%25s\t${"JVMcpuMs"}%25s\t${"AllocatedAll"}%25s\t${"AllocatedJVM"}%25s")
      println(f"$title\n\n${"RunName"}%25s\t${"AllWallMS"}%25s\t${"CPU_MS"}%25s\t${"Allocated"}%25s")
    }

    heading("ALL")
    results.foreach { case (config, configResult) =>
      printAggResults(config, configResult.all)
    }

//    heading("after 10 90%")
//    results.foreach { case (config, configResult) =>
//      printAggResults(config, configResult.filterIteration(10, 10000).std)
//    }
//
//    heading("after 10 90% JVM, no GC")
//    results.foreach { case (config, configResult) =>
//      printAggResults(config, configResult.filterIteration(10, 10000).filterPhases("jvm").filterNoGc.std)
//    }

  }

  private val lastBuiltScalac = mutable.Map[Path, String]()
  def executeRuns(envConfig: EnvironmentConfig, testConfig: TestConfig, repeat: Int): RunResult = {
    val (dir, reused) = testConfig match {
      case TestConfig(id, BuildFromGit(sha,customDir), _, _) =>
        val dir = customDir.getOrElse(envConfig.checkoutDir)
        val reused = lastBuiltScalac.get(dir).contains(sha)
        lastBuiltScalac(dir) = sha
        (dir, reused)
      case TestConfig(_, BuildFromDir(dir), _, _) =>
    (dir, lastBuiltScalac.contains(dir))

    }
    val action = if (reused) "REUSED" else "building"
    println("\n\n******************************************************************************************************")
    println(s"EXECUTING RUN ${testConfig.id} - ${testConfig.buildDefn}      $action")
    println("******************************************************************************************************\n\n")

    if (!reused) rebuildScalaC(testConfig.buildDefn, dir)
    val profileOutputFile = envConfig.outputDir / s"run_${testConfig.id}.csv"

//    executeTest(envConfig, testConfig, profileOutputFile, repeat)
    ResultReader.readResults(testConfig, profileOutputFile, repeat)
  }

  def rebuildScalaC(buildDefn:BuildType, dir: Path): Unit = {
    buildDefn match {
      case BuildFromGit(hash, _) =>
        %%("git", "fetch")(dir)
        %%("git", "reset", "--hard", hash)(dir)
        %%("git", "cherry-pick", "e929236a4b419412fda44639bbe06313fc7c05bb")(dir) //profiler
      case _ =>
    }
    runSbt(List("""set scalacOptions in Compile in ThisBuild += "optimise" """, "dist/mkPack"), dir, Nil)
  }

  def executeTest(envConfig: EnvironmentConfig, testConfig: TestConfig, profileOutputFile:Path, repeats: Int): Unit = {
    val mkPackPath = envConfig.checkoutDir / "build" / "pack"
    println("Logging stats to " + profileOutputFile)
    if (Files.exists(profileOutputFile.toNIO))
      Files.delete(profileOutputFile.toNIO)
    val extraArgsStr = if (testConfig.extraArgs.nonEmpty) testConfig.extraArgs.mkString("\"", "\",\"", "\",") else ""

    val args = List(s"++2.12.1=$mkPackPath", //"-debug",
      s"""set scalacOptions in Compile in ThisBuild ++=List($extraArgsStr"-Yprofile-destination","$profileOutputFile")""") ++
      List.fill(repeats)(List("clean", "akka-actor/compile")).flatten
    runSbt(args, envConfig.testDir, testConfig.extraJVMArgs)
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
