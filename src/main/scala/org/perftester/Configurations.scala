package org.perftester

object Configurations {
  def namesList: String = configurations.keys.mkString(",")

  val configurations: Map[String, List[TestConfig]] = Map(
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
    "perf1" -> List(
      TestConfig(s"baseline", BuildFromGit("d1b745c2e97cc89e5d26b8f5a5696a2611c01af7")),
      TestConfig(s"ownerChain", BuildFromGit("18fba7f906523dc9363dde1366c4f68d1cbe8954"))
    ),
    "ownerChain" -> ((0 to 5).toList flatMap { x =>
      List(
        TestConfig(s"0${x}_baseline", BuildFromGit("d1b745c2e97cc89e5d26b8f5a5696a2611c01af7")),
        TestConfig(s"0${x}_ownerChain", BuildFromGit("18fba7f906523dc9363dde1366c4f68d1cbe8954"))
      )
    }),
    "Piotr" -> ((0 to 9).toList flatMap { x =>
      List(
        TestConfig(s"0${x}_quick", BuildFromDir("S:/scala/quick", false)),
        TestConfig(s"0${x}_before", BuildFromGit("d1b745c2e97cc89e5d26b8f5a5696a2611c01af7")),
        TestConfig(s"0${x}_after", BuildFromGit("ac94829a4bb93bb5299564c340390b9be60932cd"))
      )
    }),
    "2121vs212x" -> List(
      TestConfig("00_2.12.2", BuildFromGit("21d12e9f5ec1ffe023f509848911476c1552d06f")),
      TestConfig("00_2.12.x", BuildFromGit("e1e8d050deb643ca56db1549e2e5a3114572a952"))
    ),
    "distinctOptimization" -> List(
      TestConfig("00_baseline", BuildFromGit("d1b745c2e97cc89e5d26b8f5a5696a2611c01af7")),
      TestConfig("00_optimized", BuildFromGit("2671541db1107497b1163e7cde337adc5abb19ef"))
    ),
    "mikelatestRory" -> List(
      TestConfig("00_canon-baseline",
                 BuildFromGit("cbd596339f2ead382be7cb625564a1217a207ca4"),
                 extraArgs = List("-Yprofile-run-gc", "*")),
      TestConfig("00_canon-updated",
                 BuildFromGit("e12ac748cda072a42c67cfd0a0151947fc9c36e3"),
                 extraArgs = List("-Yprofile-run-gc", "*")),
      //      TestConfig("00_backend-0", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "0", "-Yprofile-run-gc", "all")),
      //      TestConfig("00_backend-1", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-Yprofile-run-gc", "all")),
      //      TestConfig("00_backend-2", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-Yprofile-run-gc", "all")),
      //      TestConfig("00_backend-3", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-Yprofile-run-gc", "all")),
      //      TestConfig("00_backend-4", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-Yprofile-run-gc", "all"))
    ),
    "quick" -> List(
      TestConfig("00_bench", BuildFromDir("S:/scala/quick", false)),
      TestConfig("01_bench", BuildFromDir("S:/scala/quick", false)),
      TestConfig("02_bench", BuildFromDir("S:/scala/quick", false)),
      TestConfig("03_bench", BuildFromDir("S:/scala/quick", false))
    ),
    "mike-Nil" -> List(
      TestConfig("before-pr", BuildFromGit("d1b745c2e97cc89e5d26b8f5a5696a2611c01af7")),
      TestConfig("Nil-Hash", BuildFromGit("cf6a1985d386ef4272e1b1c8b726be54796ae6ab")),
      TestConfig("Nil-Iterator", BuildFromGit("9fadc924d92a732bcf98a1dc020375f95013b660")),
      TestConfig("inline-tl", BuildFromGit("8bcab1b635d093661fce640b14f223eb754e7f71")),
      TestConfig("eq-Nil", BuildFromGit("88274b1feaaff8b6f64d5a80227aaf3d2ee2fde8")),
      TestConfig("List-hashcode", BuildFromGit("475b95b3626f51b063bbb00f1f12c206f478e91d"))
    ),
    "COMPILER" -> List(
      TestConfig("run_sbt", BuildFromGit("d1b745c2e97cc89e5d26b8f5a5696a2611c01af7")),
      TestConfig("run_direct",
                 BuildFromGit("d1b745c2e97cc89e5d26b8f5a5696a2611c01af7"),
                 useSbt = false)
    ),
    "quick-dan4" -> List(
      TestConfig("baseline",
        BuildFromGit("8120af87ca0dcffbe5d6ebaf11bb0245e75733c2"),
        useSbt = false),
      TestConfig("duplicated",
        BuildFromGit("569c71e18b97b5644e3e62c36dc978b60c68e3c1"),
        useSbt = false),
      TestConfig("unneeded",
        BuildFromGit("2194cbd6629ad2c5348c81a45b926e29c25559a8"),
        useSbt = false),
      TestConfig("privateWithin",
        BuildFromGit("e9eda17ee47e30cc64d2dc3d49f0f29f1b1a64ac"),
        useSbt = false),
      TestConfig("annotations",
        BuildFromGit("9f6369f38b92137b8abf3008b8bcea90c8cc1a93"),
        useSbt = false),
      TestConfig("flag-usage",
        BuildFromGit("89e60d986a109b2359489e5e6bbd8bb6b09b06b1"),
        useSbt = false),
      TestConfig("scope-filtering",
        BuildFromGit("086123f0f96489be8c73c1d0e8103fde15142447"),
        useSbt = false)
    )
  )
}
