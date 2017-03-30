package org.perftester

import java.io.File
import java.nio.file.Files

import ammonite.ops.{%%, Path}
import org.perftester.results.{ResultReader, RunResult}

import scala.collection.mutable


object ProfileMain {

  def main(args: Array[String]): Unit = {
    //    readResults(TestConfig("001","xxx",Nil),1,Path("/workspace/perf_tester/src/test/resources/data/run_00_baseline_1.csv"))
    if (args.length != 3) {
      println("Usage: ProfileMain <checkoutDir> <testDir> <outputDir>")
      System.exit(1)
    }
    val checkoutDir = Path(new File(args(0)).getAbsolutePath)
    val testDir = Path(new File(args(1)).getAbsolutePath)
    val outputDir = Path(new File(args(2)).getAbsolutePath)
    val envConfig = EnvironmentConfig(checkoutDir, testDir, outputDir, 60)
    runBenchmark(envConfig)
  }

  val isWindows = System.getProperty("os.name").startsWith("Windows")

  def printAggResults(testConfig: TestConfig, results: RunResult#Detail): Unit = {
    val allWallClockTimeAvg = results.allWallClockMS
//    val jvmWallClockTimeAvg = results.phaseWallClockMS("jvm")

    val allCpuTimeAvg = results.allCPUTime
//    val jvmCpuTimeAvg = results.phaseCpuMS("jvm")

    val allAllocatedBytes = results.allAllocated
//    val jvmAllocatedBytes = results.phaseAllocatedBytes("jvm")

    val allWallMsStr = allWallClockTimeAvg.formatted(6,2)
//    val jvmWallMsStr = jvmWallClockTimeAvg.formatted(6,2)
    val allCpuMsStr = allCpuTimeAvg.formatted(6,2)
//    val jvmCpuMsStr = jvmCpuTimeAvg.formatted(6,2)
    val allAllocatedBytesStr = allAllocatedBytes.formatted(6,2)
//    val jvmAllocatedBytesStr = jvmAllocatedBytes.formatted(6,2)

//    println(f"${testConfig.id}%25s\t$allWallMsStr%25s\t$jvmWallMsStr%25s\t$allCpuMsStr%25s\t$jvmCpuMsStr%25s\t$allAllocatedBytesStr%25s\t$jvmAllocatedBytesStr%25s")
    println(f"${testConfig.id}%25s\t$allWallMsStr%25s\t$allCpuMsStr%25s\t$allAllocatedBytesStr%25s")

  }

  def runBenchmark(envConfig: EnvironmentConfig): Unit = {
    val commitsWithId = List(
      TestConfig("00_baseline", BuildFromGit("cdfba554003cb41ad8b4def46662c7379955eabb")),
      TestConfig("01_genBcodeBaseDisabled", BuildFromGit("7e6f32edffcf34cfc0b47bb41a58666ff0e7b873"), List("-YgenBcodeParallel:false")),
      TestConfig("02_genBCodeEnabled", BuildFromGit("7e6f32edffcf34cfc0b47bb41a58666ff0e7b873"), List("-YgenBcodeParallel:true")),
      TestConfig("03_genBcodeBaseDisabled_BT", BuildFromGit("45bc7a777f50ad667a8831cc91b9c963b1f9abce"), List("-YgenBcodeParallel:false")),
      TestConfig("04_genBCodeEnabled_BT", BuildFromGit("45bc7a777f50ad667a8831cc91b9c963b1f9abce"), List("-YgenBcodeParallel:true"))
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

    heading("after 10 90%")
    results.foreach { case (config, configResult) =>
      printAggResults(config, configResult.filterIteration(10, 10000).std)
    }

    heading("after 10 90% JVM, no GC")
    results.foreach { case (config, configResult) =>
      printAggResults(config, configResult.filterIteration(10, 10000).filterPhases("jvm").filterNoGc.std)
    }

  }

  private val lastBuiltScalac = mutable.Map[Path, String]()
  def executeRuns(envConfig: EnvironmentConfig, testConfig: TestConfig, repeat: Int): RunResult = {
    val (dir, reused) = testConfig match {
      case TestConfig(id, BuildFromGit(sha,customDir), _) =>
        val dir = customDir.getOrElse(envConfig.checkoutDir)
        val reused = lastBuiltScalac.get(dir) == Some(sha)
        lastBuiltScalac(dir) = sha
        (dir, reused)
      case TestConfig(id, BuildFromDir(dir), _) =>
    (dir, lastBuiltScalac.contains(dir))

    }
    val action = if (reused) "REUSED" else "building"
    println("\n\n******************************************************************************************************")
    println(s"EXECUTING RUN ${testConfig.id} - ${testConfig.buildDefn}      $action")
    println("******************************************************************************************************\n\n")

    if (!reused) rebuildScalaC(testConfig.buildDefn, dir)
    val profileOutputFile = envConfig.outputDir / s"run_${testConfig.id}.csv"

    executeTest(envConfig, testConfig, profileOutputFile, repeat)
    ResultReader.readResults(testConfig, profileOutputFile, repeat)
  }

  def rebuildScalaC(buildDefn:BuildType, dir: Path): Unit = {
    buildDefn match {
      case BuildFromGit(hash, _) =>
        %%("git", "fetch")(dir)
        %%("git", "reset", "--hard", hash)(dir)
        %%("git", "cherry-pick", "bb866a7ceb47faeb605e96904e12d3b04629ffd3")(dir) //profiler
      case _ =>
    }
    runSbt(List("""set scalacOptions in Compile in ThisBuild += "optimise" """, "dist/mkPack"), dir)
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
    runSbt(args, envConfig.testDir)
  }

  val sbtCommandLine: List[String] = {
    val sbt = new File("lib/sbt-launch.jar").getAbsoluteFile
    require(sbt.exists())
    List("java", "-Xmx12G", "-XX:MaxPermSize=256m", "-XX:ReservedCodeCacheSize=128m", "-Dsbt.log.format=true", "-mx12G", "-cp", sbt.toString, "xsbt.boot.Boot")
  }

  def runSbt(command: List[String], dir: Path): Unit = {
    import collection.JavaConverters._

    val escaped = if (isWindows) command map {
      s => s.replace("\\", "\\\\").replace("\"", "\\\"")
    } else command

    val fullCommand = (sbtCommandLine ::: escaped)
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
