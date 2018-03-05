package org.perftester

object Configurations {
  def namesList: String = configurations.keys.mkString(",")

  val configurations: Map[String, List[TestConfig]] = Map(
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
    "mike2-disk-final" -> ((0 to 2).toList flatMap { x =>
      List(
        TestConfig(s"0${x}_before-pr", BuildFromGit("09ebc826968be42faa488070826cd12a02b8f1e8")),
        TestConfig(
          s"0${x}_backend-1",
          BuildFromDir("S:/scala/backend", false),
          extraArgs = List("-Ybackend-parallelism", "1")
        ),
        TestConfig(
          s"0${x}_backend-2",
          BuildFromDir("S:/scala/backend", false),
          extraArgs = List("-Ybackend-parallelism", "2")
        ),
        TestConfig(
          s"0${x}_backend-4",
          BuildFromDir("S:/scala/backend", false),
          extraArgs = List("-Ybackend-parallelism", "4")
        ),
        TestConfig(
          s"0${x}_backend-6",
          BuildFromDir("S:/scala/backend", false),
          extraArgs = List("-Ybackend-parallelism", "6")
        ),
        TestConfig(
          s"0${x}_backend-8",
          BuildFromDir("S:/scala/backend", false),
          extraArgs = List("-Ybackend-parallelism", "8")
        ),
        TestConfig(
          s"0${x}_backend-10",
          BuildFromDir("S:/scala/backend", false),
          extraArgs = List("-Ybackend-parallelism", "10")
        ),
        TestConfig(
          s"0${x}_backend-12",
          BuildFromDir("S:/scala/backend", false),
          extraArgs = List("-Ybackend-parallelism", "12")
        )
      )
    }),
    "mike2" -> ((0 to 2).toList flatMap { x =>
      List(
        TestConfig(s"0${x}_before-pr", BuildFromGit("09ebc826968be42faa488070826cd12a02b8f1e8")),
        TestConfig(
          s"0${x}_backend-1",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "1", "-Yprofile-run-gc", "all")
        ),
        TestConfig(
          s"0${x}_backend-2",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "2", "-Yprofile-run-gc", "all")
        ),
        TestConfig(
          s"0${x}_backend-3",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "3", "-Yprofile-run-gc", "all")
        ),
        TestConfig(
          s"0${x}_backend-4",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "4", "-Yprofile-run-gc", "all")
        ),
        TestConfig(
          s"0${x}_backend-5",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "5", "-Yprofile-run-gc", "all")
        ),
        TestConfig(
          s"0${x}_backend-6",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "6", "-Yprofile-run-gc", "all")
        ),
        TestConfig(
          s"0${x}_backend-7",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "7", "-Yprofile-run-gc", "all")
        ),
        TestConfig(
          s"0${x}_backend-8",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "8", "-Yprofile-run-gc", "all")
        ),
        TestConfig(
          s"0${x}_backend-9",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "9", "-Yprofile-run-gc", "all")
        ),
        TestConfig(
          s"0${x}_backend-10",
          BuildFromGit("7e13231c56593c92fc01374146140bd5cbb56d79"),
          extraArgs = List("-YaddBackendThreads", "10", "-Yprofile-run-gc", "all")
        )
      )
    }),
    "mikeCheck" -> List(
      TestConfig(s"00_before-pr", BuildFromGit("4a03b9920e67ae2bac47d4a02656791b0535c9d3")),
      TestConfig(
        s"00_backend-0S-0-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "0",
                         "-YIoWriterThreads",
                         "0",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-0A-0-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "-1",
                         "-YIoWriterThreads",
                         "0",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-0A-1-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "-1",
                         "-YIoWriterThreads",
                         "1",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-0A-2-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "-1",
                         "-YIoWriterThreads",
                         "2",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-0A-3-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "-1",
                         "-YIoWriterThreads",
                         "3",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-1-0-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "1",
                         "-YIoWriterThreads",
                         "0",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-1-1-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "1",
                         "-YIoWriterThreads",
                         "1",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-1-2-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "1",
                         "-YIoWriterThreads",
                         "2",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-1-3-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "1",
                         "-YIoWriterThreads",
                         "3",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-2-0-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "2",
                         "-YIoWriterThreads",
                         "0",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-2-1-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "2",
                         "-YIoWriterThreads",
                         "1",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-2-2-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "2",
                         "-YIoWriterThreads",
                         "2",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-3-0-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "3",
                         "-YIoWriterThreads",
                         "0",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-3-1-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "3",
                         "-YIoWriterThreads",
                         "1",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      ),
      TestConfig(
        s"00_backend-3-2-0",
        BuildFromDir("S:/scala/backend", false),
        extraArgs = List("-YaddBackendThreads",
                         "3",
                         "-YIoWriterThreads",
                         "2",
                         "-YosIoThreads",
                         "0",
                         "-Yprofile-run-gc",
                         "all")
      )
    )
  )
}
