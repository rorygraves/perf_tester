package org.perftester

import java.io.File

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
    val envConfig = EnvironmentConfig(checkoutDir, testDir, outputDir, 10)
    runBenchmark(envConfig)
  }

  def printAggResults(testConfig: TestConfig, results: Seq[RunResult]): Unit = {
    val count = results.size
    val allWallClockTimeAvg =results.map(_.data.allWallClockMS).sum / count
    val jvmWallClockTimeAvg =results.map(_.data.phaseWallClockMS(25)).sum / count

    val jvmCpuTimeAvg =results.map(_.data.phaseCPUMS(25)).sum / count

    val allAllocatedBytes =results.map(_.data.allAllocated).sum / count
    val jvmAllocatedBytes =results.map(_.data.phaseAllocatedBytes(25)).sum / count

    val allWallMsStr = f"$allWallClockTimeAvg%6.2f"
    val jvmWallMsSTr = f"$jvmWallClockTimeAvg%6.2f"
    val jvmCpuMsSTr = f"$jvmCpuTimeAvg%6.2f"

    println(f"${testConfig.id}%25s\t$allWallMsStr\t$jvmWallMsSTr\t\t$jvmCpuMsSTr\t\t$allAllocatedBytes\t\t$jvmAllocatedBytes")

  }

  def runBenchmark(envConfig: EnvironmentConfig): Unit = {
    val commitsWithId = List(
      // ("01_baseline", "b09b7feca8c18bfb49c24cc88e94a99703474678"), // baseline
      // ("02_applied", "920bc4e31c5415d98c1a7f26aebc790250aafe4a") // opts


      TestConfig("00_baseline", "147e5dd1b88a690b851e57a1783f099cb0dad091"),
      TestConfig("01_genBcodeBaseDisabled", "4b283eb20c7365ddbdee0239cddce1bb96981ec3", List("-YgenBcodeParallel:false")),
      TestConfig("02_genBCodeEnabled", "4b283eb20c7365ddbdee0239cddce1bb96981ec3", List("-YgenBcodeParallel:true"))
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

    println(f"\n\n${"RunName"}%25s\tAllWallMS\tJVMWallMS\tJVMUserMS\tJVMcpuMs\tAllocatedAll\tAllocatedJVM")

    results.foreach {  case (config, configResult) =>
      printAggResults(config, configResult)
    }

  }

  def executeRuns(envConfig: EnvironmentConfig, testConfig: TestConfig, repeat: Int): Seq[RunResult] = {
    println("\n\n******************************************************************************************************")
    println(s"EXECUTING RUN ${testConfig.id} - ${testConfig.commit}")
    println("******************************************************************************************************\n\n")
    rebuildScalaC(testConfig.commit, envConfig.checkoutDir)
    (1 to repeat) map { i =>
      println(s" run $i")
      executeTest(envConfig, testConfig, i)
    }
  }

  def rebuildScalaC(hash: String, checkoutDir: Path): Unit = {
    %%("git", "reset", "--hard", hash)(checkoutDir)
    %%("git", "cherry-pick", "534d37e8f73fd42ba88b4e49f75af76c7533ae66")(checkoutDir)
    runSbt(List("""set scalacOptions in Compile in ThisBuild += "optimise" """, "dist/mkPack"),checkoutDir )
  }

  def executeTest(envConfig: EnvironmentConfig, testConfig: TestConfig, iteration: Int): RunResult = {
    val mkPackPath = envConfig.checkoutDir / "build" / "pack"
    val profileOutputFile = envConfig.outputDir / s"run_${testConfig.id}_$iteration.csv"
    println("Logging stats to " + profileOutputFile)
    val extraArgsStr = if (testConfig.extraArgs.nonEmpty) testConfig.extraArgs.mkString("\"", "\",\"", "\",") else ""

    val args = List(s"++2.12.1=$mkPackPath", //"-debug",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      s"""set scalacOptions in Compile in ThisBuild ++=List($extraArgsStr"-Yprofile-destination","$profileOutputFile")""",
      "clean", "akka-actor/compile")
    runSbt(args, envConfig.testDir)
    ResultReader.readResults(testConfig, iteration, profileOutputFile)
  }
  val sbtCommandLine: List[String] = {
    val sbt = new File("lib/sbt-launch.jar").getAbsoluteFile
    require(sbt.exists())
    List("java", "-Xmx12G", "-XX:MaxPermSize=256m", "-XX:ReservedCodeCacheSize=128m", "-Dsbt.log.format=true", "-mx12G", "-cp", sbt.toString, "xsbt.boot.Boot")
  }

  def runSbt(command:List[String], dir: Path) : Unit = {
//    val escaped = command map {
//      s => s.replace("\\", "\\\\").replace("\"", "\\\"")
//    }

    val escaped = command

    import collection.JavaConverters._
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
