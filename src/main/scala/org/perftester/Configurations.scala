package org.perftester

import org.perftester.git.GitUtils

object Configurations {
  def configurationsFor(envConfig: EnvironmentConfig): Map[String, () => List[TestConfig]] =
    configurations map {
      case (name, generator) => name -> (() => generator(envConfig))
    }

  def namesList: String = configurations.keys.mkString(",")

  def eachStep(baseBranch: String,
               testBranch: String,
               config: EnvironmentConfig): List[(String, BranchRevision)] = {
    val git = GitUtils(config.checkoutDir.toIO)
    val revisions = try {
      git
        .branchRevisions(s"refs/remotes/$baseBranch", s"refs/remotes/$testBranch")
        .zipWithIndex
        .map {
          case (rev, i) => BranchRevision(i, rev)
        }
    } finally git.dispose()
    val safeId = testBranch.replace('/', '_')
    revisions map {
      case revision if revision.index == 0 => (s"${safeId}-base-${revision.index}", revision)
      case revision                        => (s"${safeId}-${revision.index}", revision)
    }
  }

  def individually(baseBranch: String,
                   testBranch: String,
                   extraArgs: List[String] = Nil,
                   extraJVMArgs: List[String] = Nil,
                   useSbt: Boolean = true)(config: EnvironmentConfig): List[TestConfig] = {
    val steps    = eachStep(baseBranch, testBranch, config)
    val baseline = steps.head._2
    steps.map {
      case (name, rev) if (rev == baseline) =>
        TestConfig(name,
                   BuildFromGit(baseSha = baseline.sha),
                   extraJVMArgs = extraArgs,
                   extraArgs = extraArgs,
                   useSbt = useSbt)
      case (name, rev) if (rev == baseline) =>
        val cherryPicks = List(rev.sha)
        TestConfig(s"${name}.baseline+${rev.sha}",
                   BuildFromGit(baseSha = baseline.sha, cherryPicks = cherryPicks),
                   extraJVMArgs = extraArgs,
                   extraArgs = extraArgs,
                   useSbt = useSbt)
    }
  }

  def series(baseBranch: String,
             testBranch: String,
             extraArgs: List[String] = Nil,
             extraJVMArgs: List[String] = Nil,
             useSbt: Boolean = true)(config: EnvironmentConfig): List[TestConfig] = {
    val steps    = eachStep(baseBranch, testBranch, config)
    val baseline = steps.head._2
    steps.map {
      case (name, rev) if (rev == baseline) =>
        TestConfig(name,
                   BuildFromGit(baseSha = baseline.sha),
                   extraJVMArgs = extraJVMArgs,
                   extraArgs = extraArgs,
                   useSbt = useSbt)
      case (name, rev) =>
        TestConfig(s"${name}=${rev.sha}",
                   BuildFromGit(baseSha = rev.sha),
                   extraJVMArgs = extraJVMArgs,
                   extraArgs = extraArgs,
                   useSbt = useSbt)
    }
  }
  def lastOnly(baseBranch: String,
               testBranch: String,
               extraArgs: List[String] = Nil,
               extraJVMArgs: List[String] = Nil,
               useSbt: Boolean = true)(config: EnvironmentConfig): List[TestConfig] = {
    val steps                       = eachStep(baseBranch, testBranch, config)
    val (baselineName, baselineRev) = steps.head
    val (_, lastRev)                = steps.last
    val lastName                    = s"${testBranch.substring(testBranch.lastIndexOf('/') + 1)}=${lastRev.sha}"
    List(
      TestConfig(baselineName,
                 BuildFromGit(baseSha = baselineRev.sha),
                 extraJVMArgs = extraJVMArgs,
                 extraArgs = extraArgs,
                 useSbt = useSbt),
      TestConfig(lastName,
                 BuildFromGit(baseSha = lastRev.sha),
                 extraJVMArgs = extraJVMArgs,
                 extraArgs = extraArgs,
                 useSbt = useSbt)
    )
  }
  def branchLatest(baseBranch: String,
                   testBranch: String,
                   extraArgs: List[String] = Nil,
                   extraJVMArgs: List[String] = Nil,
                   useSbt: Boolean = true)(config: EnvironmentConfig): List[TestConfig] = {
    val steps                       = eachStep(baseBranch, testBranch, config)
    val (baselineName, baselineRev) = steps.head
    val (_, lastRev)                = steps.last
    val lastName                    = s"${testBranch.substring(testBranch.lastIndexOf('/') + 1)}=${lastRev.sha}"
    List(
      TestConfig(lastName,
                 BuildFromGit(baseSha = lastRev.sha),
                 extraJVMArgs = extraJVMArgs,
                 extraArgs = extraArgs,
                 useSbt = useSbt)
    )
  }
  private val dynmanicConfiguration: Map[String, (EnvironmentConfig) => List[TestConfig]] = Map(
    "miles-byname-implicits" -> (
        (_: EnvironmentConfig) =>
          List(
            TestConfig("pre-pr", BuildFromGit("53f06bcc786caeeb360a5c60ca571b84f50ea2ab")),
//        TestConfig("pr", BuildFromGit("063a508cee9e50ac712f998182e7f806c30382d0")),
//        TestConfig("jason-feedback", BuildFromGit("563930783121cfce86f13e2dcb9d0f09927a03ab"))
          )),
    "quick-dan4" -> series("scala/2.12.x", "dan/2.12.x_flag", useSbt = false),
    "quick-13-imports" -> series("scala/2.13.x",
                                 "origin/mike/2.13.x_implicit_import",
                                 useSbt = true),
    "quick-devxx"    -> lastOnly("scala/2.12.x", "origin/mike/2.12.x_developer", useSbt = false),
    "213x_postTyper" -> lastOnly("scala/2.13.x", "origin/mike/2.13.x_postTyper", useSbt = false),
    "212x_rangepos" -> series("scala/2.12.x",
                              "origin/mike/2.12.x_rangepos",
                              useSbt = false,
                              extraArgs = List("-Yrangepos")),
    "212x_rangepos_last" -> branchLatest(
      "scala/2.12.x",
      "origin/mike/2.12.x_rangepos",
      useSbt = false,
      extraArgs = List("-Yrangepos"),
      extraJVMArgs = List(
        "-XX:+UnlockCommercialFeatures",
        "-XX:+FlightRecorder",
        "-XX:FlightRecorderOptions=stackdepth=4096",
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:+DebugNonSafepoints"
      )
    ),
    "212x_rangepos_pr" -> series("scala/2.12.x",
                                 "origin/mike/2.12.x_rangepos_pr",
                                 useSbt = false,
                                 extraArgs = List("-Yrangepos")),
    "quick-dev" -> ((_: EnvironmentConfig) =>
      List(
        TestConfig("baseline",
                   BuildFromGit("30a1428925497a7358fd386db84fd982c3108707"),
                   extraJVMArgs = List("-XX:MaxInlineLevel=32"),
                   useSbt = false),
//        TestConfig("guarded-debug",
//                   BuildFromGit("8f6b9ce1a529bbda8ba2fa8b912ac61124880d98"),
//                   extraJVMArgs = List("-XX:MaxInlineLevel=32"),
//                   useSbt = true),
        TestConfig("preferred",
                   BuildFromGit("dde6abc6d844c6ecc4e1acaca3501cc686723c28"),
                   extraJVMArgs = List("-XX:MaxInlineLevel=32"),
                   useSbt = false),
        TestConfig("noop",
                   BuildFromGit("3cce6c792bceeff3ed1bfea968ee7d3b36348a8c"),
                   extraJVMArgs = List("-XX:MaxInlineLevel=32"),
                   useSbt = false)
      ))
  )

  private val staticConfiguration: Map[String, List[TestConfig]] = Map(
    "quick-dan" -> List(
      TestConfig("baseline",
                 BuildFromGit("cb5f0fc1ba5eb593c88de5b341d382aef6b61d72"),
                 extraJVMArgs = List("-XX:MaxInlineLevel=32"),
                 useSbt = true),
      TestConfig("duplicated",
                 BuildFromGit("8b116c47f78d9ba912ba47be5ceaeb77f6c957cc"),
                 extraJVMArgs = List("-XX:MaxInlineLevel=32"),
                 useSbt = true),
      TestConfig("unneeded",
                 BuildFromGit("6b0961f50a28aa77273081b9e0df5668a5ac8d8a"),
                 extraJVMArgs = List("-XX:MaxInlineLevel=32"),
                 useSbt = true),
      TestConfig("privateWithin",
                 BuildFromGit("dc90bd8d0d063f29d1b2b6e1ded34a64b1891c4e"),
                 extraJVMArgs = List("-XX:MaxInlineLevel=32"),
                 useSbt = true),
      TestConfig("annotations",
                 BuildFromGit("577c776fdc69ba02e78346b3a988a9c7d4a28a0a"),
                 extraJVMArgs = List("-XX:MaxInlineLevel=32"),
                 useSbt = true)
    ),
    "212x" -> List(
      TestConfig("00_1.12.x", BuildFromGit("ea64efaf0a71827128772585731df7635b871699"))
    ),
    "212x_settings" -> List(
      TestConfig("00_1.12.x", BuildFromDir("s:/scala/backend"))
    ),
  )
  val configurations: Map[String, (EnvironmentConfig) => List[TestConfig]] = {
    val dynamicFromStatic = staticConfiguration map {
      case (name, config) =>
        assert(!dynmanicConfiguration.contains(name), s"both configurations contain $name")
        name -> ((_: EnvironmentConfig) => config)
    }
    dynmanicConfiguration ++ dynamicFromStatic
  }
}
