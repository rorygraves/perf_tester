import java.io.File
import java.lang.ProcessBuilder.Redirect
import java.lang.ProcessBuilder.Redirect.Type

import ProfileMain.TestConfig
import ammonite.ops.{%%, Path, read}

/**
  * Created by rorygraves on 09/03/2017.
  */
object ProfileMain {

  case class EnvironmentConfig(checkoutDir: Path, testDir: Path, outputDir: Path)

  case class TestConfig(id: String, commit: String, extraArgs: List[String] = Nil)

  def main(args: Array[String]): Unit = {
//    readResults(Path("/workspace/perf_tester/src/test/resources/data/run_00_baseline_1.csv"))
    if (args.length != 3) {
      println("Usage: ProfileMain <checkoutDir> <testDir> <outputDir>")
      System.exit(1)
    }
    val checkoutDir = Path(new File(args(0)).getAbsolutePath)
    val testDir = Path(new File(args(1)).getAbsolutePath)
    val outputDir = Path(new File(args(2)).getAbsolutePath)
    val envConfig = EnvironmentConfig(checkoutDir, testDir, outputDir)
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

    val allAllocatedBytesStr = f"$allAllocatedBytes%6d"
    val jvmAllocatedBytesStr = f"$jvmAllocatedBytes%6d"

    println(f"${testConfig.id}%25s\t$allWallMsStr\t$jvmWallMsSTr\t\t$jvmCpuMsSTr\t\t$allAllocatedBytes\t\t$jvmAllocatedBytes")

  }

  def runBenchmark(envConfig: EnvironmentConfig): Unit = {

    //val commits = getRevisions(hash, checkoutDir)

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
      val results = executeRuns(envConfig, testConfig, 20)
      (testConfig, results)
    }

    println(f"\n\n${"RunName"}%25s\tAllWallMS\tJVMWallMS\tJVMUserMS\tJVMcpuMs\tAllocatedAll\tAllocatedJVM")

    results.foreach {  case (config, results) =>
      printAggResults(config, results)
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
//    %%("git", "cherry-pick", "69bfa259f0f5876b7361853e9a517d9901ae45fb")(checkoutDir)
//    %%("git", "cherry-pick", "dbc61410934d19482e5fe7b7e8f8810d64f85782")(checkoutDir)
    %%("git", "cherry-pick", "a7b49706d112a0d7740755938863db395cfb8466")(checkoutDir)
    runSbt(List("""set scalacOptions in Compile in ThisBuild += "optimise" """, "dist/mkPack"),checkoutDir )
//    %%("sbt.bat", """set scalacOptions in Compile in ThisBuild += ""optimise"" """, "dist/mkPack")(checkoutDir)
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
    readResults(testConfig, iteration, profileOutputFile)
  }
  val sbtCommandLine = {
    val sbt = new File("lib/sbt-launch.jar").getAbsoluteFile
    require(sbt.exists())
    List("java", "-Xmx12G", "-XX:MaxPermSize=256m", "-XX:ReservedCodeCacheSize=128m", "-Dsbt.log.format=true", "-mx12G", "-cp", sbt.toString, "xsbt.boot.Boot")
  }
  def runSbt(command:List[String], dir: Path) : Unit = {
    val escaped = command map {
      s => s.replace("\\", "\\\\").replace("\"", "\\\"")
    }
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

  def executeTestOld(envConfig: EnvironmentConfig, testConfig: TestConfig, iteration: Int): RunResult = {
    val mkPackPath = envConfig.checkoutDir / "build" / "pack"
    val profileOutputFile = envConfig.outputDir / s"run_${testConfig.id}_$iteration.csv"
    val profileOutputFileEsc = profileOutputFile.toString().replace("\\", "/")


//    var profileOutputFile = Path("/workspace/perf_tester/src/test/resources/data/")/ s"run_${testConfig.id}_$iteration.csv"


    println("Logging stats to " + profileOutputFile)
    println("Escaped" + profileOutputFileEsc)
    val extraArgsStr = if (testConfig.extraArgs.nonEmpty) testConfig.extraArgs.mkString("\"\"", "\"\",\"\"", "\"\",") else ""
    val args = List("sbt.bat", s"++2.12.1=$mkPackPath",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      s"""set scalacOptions in Compile in ThisBuild ++=List($extraArgsStr""-Yprofile-destination"",""$profileOutputFileEsc"") """,
      "clean", "akka-actor/compile"
    )

    val displayString = args.mkString("\"", """","""", "\"")
    println(s"Command line = ${displayString}")

    %%("sbt.bat", s"""++2.12.1=s:/scala/test/scalac/build/pack""", //$mkPackPath",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      s"""set scalacOptions in Compile in ThisBuild ++=List($extraArgsStr""-Yprofile-destination"",""$profileOutputFileEsc"")""",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile",
      "clean", "akka-actor/compile")(envConfig.testDir)

    readResults(testConfig, iteration, profileOutputFile)
  }

  case class PhaseData(phaseId: Int,
                       phaseName: String, wallClockTimeMS : Double, cpuTimeMS : Double,
                       userTimeMS : Double, allocatedBytes: Long)
  case class PhaseSet(phases: List[PhaseData]) {
    def phaseAllocatedBytes(phaseId: Int) = byPhaseId(phaseId).allocatedBytes

    def phaseWallClockMS(phaseId: Int) = byPhaseId(phaseId).wallClockTimeMS

    def phaseCPUMS(phaseId: Int) = byPhaseId(phaseId).cpuTimeMS

    val byPhaseId = phases.map( p => (p.phaseId, p)).toMap
    def allWallClockMS = phases.map(_.wallClockTimeMS).sum
    def allAllocated = phases.map(_.allocatedBytes).sum
    def allCPUTime = phases.map(_.cpuTimeMS).sum

  }
  case class RunResult(testConfig: TestConfig, iteration: Int, data: PhaseSet)

//  phase, phaseName, wallClockTimeNs,wallClockTimeMs, cpuTimeNs, cpuTimeMs, userTimeNs, allocatedBytes, retainedHeapBytes, gcTimeMs

  def readResults(testConfig: TestConfig, iteration: Int, file : Path): RunResult = {
    val lines = read.lines! file
    val asValues = lines.map(_.split(',').toList)
    val dataLines = asValues.filter(_.head == "data")
    val rows = dataLines.map { row =>
      PhaseData(
        // data,
        row(1).toInt, // phaseId
        row(2), // phaseName
        // wallClockTimeNs
        row(4).toDouble, // wallClockTimeMs,
        // cpuTimeNs,
        row(6).toDouble, // cpuTimeMs,
        row(7).toDouble / 1e6,// userTimeNs,
        row(8).toLong// allocatedBytes, retainedHeapBytes, gcTimeMs

      )
    }
    RunResult(testConfig,iteration,PhaseSet(rows.toList))
  }

  def getRevisions(base: String, checkoutDir: Path): List[String] = {
    val res = %%("git", "rev-list", "--no-merges", s"$base..HEAD")(checkoutDir)
    val commits = res.out.lines.toList.reverse
    commits
  }

}
