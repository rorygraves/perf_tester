package org.perftester

import java.io.File
import java.nio.file.Files

import ammonite.ops.{%%, Path}
import org.perftester.results.{ResultReader, RunResult}


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
    val jvmWallClockTimeAvg = results.phaseWallClockMS("jvm")

    val allCpuTimeAvg = results.allCPUTime
    val jvmCpuTimeAvg = results.phaseCpuMS("jvm")

    val allAllocatedBytes = results.allAllocated
    val jvmAllocatedBytes = results.phaseAllocatedBytes("jvm")

    val allWallMsStr = allWallClockTimeAvg.formatted(6,2)
    val jvmWallMsStr = jvmWallClockTimeAvg.formatted(6,2)
    val allCpuMsStr = allCpuTimeAvg.formatted(6,2)
    val jvmCpuMsStr = jvmCpuTimeAvg.formatted(6,2)
    val allAllocatedBytesStr = allAllocatedBytes.formatted(6,2)
    val jvmAllocatedBytesStr = jvmAllocatedBytes.formatted(6,2)

    println(f"${testConfig.id}%25s\t$allWallMsStr%25s\t$jvmWallMsStr%25s\t$allCpuMsStr%25s\t$jvmCpuMsStr%25s\t$allAllocatedBytesStr%25s\t$jvmAllocatedBytesStr%25s")

  }

  def runBenchmark(envConfig: EnvironmentConfig): Unit = {
    val commitsWithId = List(
      // ("01_baseline", "b09b7feca8c18bfb49c24cc88e94a99703474678"), // baseline
      // ("02_applied", "920bc4e31c5415d98c1a7f26aebc790250aafe4a") // opts


      TestConfig("00_baseline", "147e5dd1b88a690b851e57a1783f099cb0dad091"),
      TestConfig("01_genBcodeBaseDisabled", "7a892a5b40d815c42a9e65ab5be09651359a544f", List("-YgenBcodeParallel:false")),
      TestConfig("02_genBCodeEnabled", "7a892a5b40d815c42a9e65ab5be09651359a544f", List("-YgenBcodeParallel:true"))
      //      TestConfig("03_genBcodeDisabledNoWrite", "529e7a52f3c601a0c5523a6174548724a612f0b8", List("-Ynowriting", "-YgenBcodeParallel:false")),
      //      TestConfig("04_genBCodeEnabledNoWrite", "529e7a52f3c601a0c5523a6174548724a612f0b8", List("-Ynowriting", "-YgenBcodeParallel:true"))


      //    ("00_bonus", "c38bb2a9168b0a02ef99a15851459c2591667b4c"), // New
      //    ("01_17Feb", "147e5dd1b88a690b851e57a1783f099cb0dad091"), // 17th feb
      //    ("02_30Jan", "6d4782774be5ffff361724e4e22a6ae61d4624fe"), // 30th Jan
      //    ("03_15Jan", "2268aabbcbc1a4ad6ac3d6cde960dfeb85ffbb5b"), // 15th Jan
      //    ("04_30Dec", "a75e4a7fafef9ce619a8d0f0622333d20502e7c8"), // 30th Dec
      //    ("05_30Nov", "0339663cbbd4d22b0758257f2ce078b5a007f316") // 30th Nov
      //      ("06_settings", "946cd11d45785caed5ad87837f66c7051b34363d") // New
    )

    val results = commitsWithId map { testConfig =>
      val results = executeRuns(envConfig, testConfig, envConfig.iterations)
      (testConfig, results)
    }

    def heading(title: String) {
      println(f"$title\n\n${"RunName"}%25s\t${"AllWallMS"}%25s\t${"JVMWallMS"}%25s\t${"JVMUserMS"}%25s\t${"JVMcpuMs"}%25s\t${"AllocatedAll"}%25s\t${"AllocatedJVM"}%25s")
    }

    heading("ALL")
    results.foreach { case (config, configResult) =>
      printAggResults(config, configResult.all)
      printAggResults(config, configResult.std)
    }

    heading("after 10")
    results.foreach { case (config, configResult) =>
      printAggResults(config, configResult.filterIteration(10, 10000).all)
      printAggResults(config, configResult.filterIteration(10, 10000).std)
    }

    heading("after 10 JVM")
    results.foreach { case (config, configResult) =>
      printAggResults(config, configResult.filterIteration(10, 10000).filterPhases("jvm").all)
      printAggResults(config, configResult.filterIteration(10, 10000).filterPhases("jvm").std)
    }

    heading("after 10 JVM, no GC")
    results.foreach { case (config, configResult) =>
      printAggResults(config, configResult.filterIteration(10, 10000).filterPhases("jvm").filterNoGc.all)
      printAggResults(config, configResult.filterIteration(10, 10000).filterPhases("jvm").filterNoGc.std)
    }

  }

  private var lastBuiltScalac:String = ""
  def executeRuns(envConfig: EnvironmentConfig, testConfig: TestConfig, repeat: Int): RunResult = {
    val reused = if (lastBuiltScalac == testConfig.commit) "REUSED" else "building"
    println("\n\n******************************************************************************************************")
    println(s"EXECUTING RUN ${testConfig.id} - ${testConfig.commit}      $reused")
    println("******************************************************************************************************\n\n")
    if (lastBuiltScalac != testConfig.commit) {
      rebuildScalaC(testConfig.commit, envConfig.checkoutDir)
      lastBuiltScalac = testConfig.commit
    }
    val profileOutputFile = envConfig.outputDir / s"run_${testConfig.id}.csv"

    executeTest(envConfig, testConfig, profileOutputFile, repeat)
    ResultReader.readResults(testConfig, profileOutputFile, repeat)
  }

  def rebuildScalaC(hash: String, checkoutDir: Path): Unit = {
    %%("git", "reset", "--hard", hash)(checkoutDir)
    %%("git", "cherry-pick", "534d37e8f73fd42ba88b4e49f75af76c7533ae66")(checkoutDir) //profiler
    %%("git", "cherry-pick", "6f9d235d3b547bd9e586f3e30981a95b45788f85")(checkoutDir) //sbt hack
    runSbt(List("""set scalacOptions in Compile in ThisBuild += "optimise" """, "dist/mkPack"), checkoutDir)
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
