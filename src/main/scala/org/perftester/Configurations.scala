package org.perftester

object Configurations {

  def namesList: String = configurations.keys.mkString(",")

  val configurations: Map[String, List[TestConfig]] = Map(
    "212x" -> List(
      TestConfig("00_1.12.x", BuildFromGit("8e6964a13035bf83d3050916e988715d23e51b49"))
    ),
    "2121vs212x" -> List(
      TestConfig("00_2.12.2", BuildFromGit("21d12e9f5ec1ffe023f509848911476c1552d06f")),
      TestConfig("00_2.12.x", BuildFromGit("e1e8d050deb643ca56db1549e2e5a3114572a952"))
    ),
    "distinctOptimization" -> List(
      TestConfig("00_baseline",
                 BuildFromGit("e12ac748cda072a42c67cfd0a0151947fc9c36e3"),
                 extraArgs = List("-Yprofile-run-gc", "all")),
      TestConfig("00_optimized",
                 BuildFromGit("c87d90a215802fcf1ef863b68fda19e99f0c9a25"),
                 extraArgs = List("-Yprofile-run-gc", "all"))
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
    "mikeGit" -> List(
      TestConfig("00_before-pr", BuildFromGit("4a03b9920e67ae2bac47d4a02656791b0535c9d3")),
      TestConfig("00_backend-S",
                 BuildFromGit("9faa169cd96afd592a1e411a8a242d3cfaa49a97"),
                 extraArgs = List("-YaddBackendThreads", "-1", "-Yprofile-run-gc", "all")),
      TestConfig("00_backend-0",
                 BuildFromGit("9faa169cd96afd592a1e411a8a242d3cfaa49a97"),
                 extraArgs = List("-YaddBackendThreads", "0", "-Yprofile-run-gc", "all")),
      TestConfig("00_backend-1",
                 BuildFromGit("9faa169cd96afd592a1e411a8a242d3cfaa49a97"),
                 extraArgs = List("-YaddBackendThreads", "1", "-Yprofile-run-gc", "all")),
      TestConfig("00_backend-2",
                 BuildFromGit("9faa169cd96afd592a1e411a8a242d3cfaa49a97"),
                 extraArgs = List("-YaddBackendThreads", "2", "-Yprofile-run-gc", "all")),
      TestConfig("00_backend-3",
                 BuildFromGit("9faa169cd96afd592a1e411a8a242d3cfaa49a97"),
                 extraArgs = List("-YaddBackendThreads", "3", "-Yprofile-run-gc", "all")),
      TestConfig("00_backend-4",
                 BuildFromGit("9faa169cd96afd592a1e411a8a242d3cfaa49a97"),
                 extraArgs = List("-YaddBackendThreads", "4", "-Yprofile-run-gc", "all"))
    ),
    "mike" -> ((1 to 5).toList flatMap { x =>
      List(
        TestConfig(s"0${x}_before-pr", BuildFromGit("4a03b9920e67ae2bac47d4a02656791b0535c9d3")),
        TestConfig(s"0${x}_backend-S",
                   BuildFromDir("S:/scala/backend", false),
                   extraArgs = List("-YaddBackendThreads", "-1", "-Yprofile-run-gc", "all")),
        TestConfig(s"0${x}_backend-0",
                   BuildFromDir("S:/scala/backend", false),
                   extraArgs = List("-YaddBackendThreads", "0", "-Yprofile-run-gc", "all")),
        TestConfig(s"0${x}_backend-1",
                   BuildFromDir("S:/scala/backend", false),
                   extraArgs = List("-YaddBackendThreads", "1", "-Yprofile-run-gc", "all")),
        TestConfig(s"0${x}_backend-2",
                   BuildFromDir("S:/scala/backend", false),
                   extraArgs = List("-YaddBackendThreads", "2", "-Yprofile-run-gc", "all")),
        TestConfig(s"0${x}_backend-3",
                   BuildFromDir("S:/scala/backend", false),
                   extraArgs = List("-YaddBackendThreads", "3", "-Yprofile-run-gc", "all")),
        TestConfig(s"0${x}_backend-4",
                   BuildFromDir("S:/scala/backend", false),
                   extraArgs = List("-YaddBackendThreads", "4", "-Yprofile-run-gc", "all"))
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
