import ammonite.ops.{%%, Path}

/**
  * Created by rorygraves on 09/03/2017.
  */
object ProfileMain {

  def main(args: Array[String]): Unit = {
    if(args.length != 3) {
      println("Usage: ProfileMain <checkoutDir> <testDir> <outputDir>")
      System.exit(1)
    }
    mainEx("aaa", Path(args(0)), Path(args(1)), Path(args(2)))
  }

  def mainEx(hash: String, checkoutDir: Path, testDir: Path, outputDir: Path): Unit = {

      //val commits = getRevisions(hash, checkoutDir)

    val commitsWithId = List(
      // ("01_baseline", "b09b7feca8c18bfb49c24cc88e94a99703474678"), // baseline
      // ("02_applied", "920bc4e31c5415d98c1a7f26aebc790250aafe4a") // opts



      ("00_baseone", "147e5dd1b88a690b851e57a1783f099cb0dad091", ,List()), // baselin
      ("01_genBcodeBaseDisabled", "4b283eb20c7365ddbdee0239cddce1bb96981ec3", ,List("-YgenBCodeParallel:false")), // baselin
      ("01_genBcodeBaseDisabled", "4b283eb20c7365ddbdee0239cddce1bb96981ec3", ,List("-YgenBCodeParallel:false")), // baselin
      ("02_genBCodeEnabled", "4b283eb20c7365ddbdee0239cddce1bb96981ec3",List("-YgenBCodeParallel:true"))

      //    ("00_bonus", "c38bb2a9168b0a02ef99a15851459c2591667b4c"), // New
      //    ("01_17Feb", "147e5dd1b88a690b851e57a1783f099cb0dad091"), // 17th feb
      //    ("02_30Jan", "6d4782774be5ffff361724e4e22a6ae61d4624fe"), // 30th Jan
      //    ("03_15Jan", "2268aabbcbc1a4ad6ac3d6cde960dfeb85ffbb5b"), // 15th Jan
      //    ("04_30Dec", "a75e4a7fafef9ce619a8d0f0622333d20502e7c8"), // 30th Dec
      //    ("05_30Nov", "0339663cbbd4d22b0758257f2ce078b5a007f316") // 30th Nov
      //      ("06_settings", "946cd11d45785caed5ad87837f66c7051b34363d") // New
    )

    commitsWithId foreach { case (id, commit, extraArgs) =>
      executeRuns(id,commit, 10, checkoutDir,testDir, outputDir, extraArgs)

    }


    // executeRun("147e5dd1b88a690b851e57a1783f099cb0dad091",checkoutDir, testDir, outputDir)

  }

  def executeRuns(id: String, hash: String,repeat: Int, checkoutDir: Path, testDir: Path, outputDir: Path,extraArgs: List[String]): Unit = {
    println("\n\n******************************************************************************************************")
    println(s"EXECUTING RUN $id - $hash")
    println("******************************************************************************************************\n\n")
    rebuildScalaC(hash, checkoutDir)
    (1 to repeat) foreach { i =>
      println(" run " + i )
      executeTest(id, hash, "" + i, checkoutDir, testDir, outputDir, extraArgs)
    }
  }

  def rebuildScalaC(hash: String, checkoutDir: Path): Unit = {
    %%("git","reset","--hard",hash)(checkoutDir)
    %%("git","cherry-pick","a7b49706d112a0d7740755938863db395cfb8466")(checkoutDir)
    %%("sbt","set scalacOptions in Compile in ThisBuild += \"optimise\"","dist/mkPack")(checkoutDir)
  }

  def executeTest(id: String, hash: String, run: String, checkoutDir: Path, testDir: Path, outputDir: Path, extraArgs: List[String]): Unit = {
    val mkPackPath = checkoutDir / "build" / "pack"
    var profileOutputFile = outputDir / s"run_${id}_${run}.csv"
    println("Logging stats to " + profileOutputFile)
    //  %("sbt",s"++2.12.1=$mkPackPath","set scalacOptions in Compile in ThisBuild +=\"-Yprofile-enabled\"","clean","akka-actor/compile")(testDir)
    %%("sbt",s"++2.12.1=$mkPackPath", "clean")(testDir)
    val argsStr = if(extraArgs.nonEmpty) extraArgs.mkString("\"", "\",\"", "\",") else ""
    //  %%("sbt",s"++2.12.1=$mkPackPath",s"""set scalacOptions in Compile in ThisBuild ++=List("-Yprofile-destination","$profileOutputFile")""","clean","akka-actor/compile")(testDir)
    %%("sbt",s"++2.12.1=$mkPackPath",s"""set scalacOptions in Compile in ThisBuild ++=List($argsStr"-Yprofile-destination","$profileOutputFile")""","clean","akka-actor/compile")(testDir)


  }

  def getRevisions(base: String, checkoutDir: Path): List[String] = {
    val res = %%("git", "rev-list", "--no-merges",s"$base..HEAD")(checkoutDir)
    val commits = res.out.lines.toList.reverse
    commits
  }

}
