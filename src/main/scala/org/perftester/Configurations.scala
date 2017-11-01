package org.perftester

object Configurations {

  def namesList: String = configurations.keys.mkString(",")

  val configurations: Map[String, List[TestConfig]] = Map(
    "212x" -> List(
      TestConfig("00_1.12.x", BuildFromGit("e1e8d050deb643ca56db1549e2e5a3114572a952"))
    ),
    "2121vs212x" -> List(
      TestConfig("00_2.12.2", BuildFromGit("21d12e9f5ec1ffe023f509848911476c1552d06f")),
      TestConfig("00_2.12.x", BuildFromGit("e1e8d050deb643ca56db1549e2e5a3114572a952"))
    ),

    "mikelatestRory" -> List(
      TestConfig("00_backend-baseline", BuildFromGit("c2a5883891a68180b143eb462c8b0cebc8d3b021"), extraArgs = List("-Yprofile-run-gc", "*")),
//      TestConfig("00_backend-0", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "0", "-Yprofile-run-gc", "all")),
//      TestConfig("00_backend-1", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-Yprofile-run-gc", "all")),
//      TestConfig("00_backend-2", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-Yprofile-run-gc", "all")),
//      TestConfig("00_backend-3", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-Yprofile-run-gc", "all")),
//      TestConfig("00_backend-4", BuildFromGit("033be3f7053f91e87329f17e46265449534e0a09"), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-Yprofile-run-gc", "all"))
    )


    //    "mike_latest" -> List(
    //        TestConfig("00_backend-baseline", BuildFromDir("S:/scala/backend-before"), extraArgs = List("-Yprofile-run-gc", "*")),
    //      TestConfig("00_backend-0", BuildFromDir("S:/scala/backend"), extraArgs = List("-YmaxAdditionalWriterThreads", "0", "-Yprofile-run-gc", "all")),
    //      TestConfig("00_backend-1", BuildFromDir("S:/scala/backend"), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-Yprofile-run-gc", "all")),
    //      TestConfig("00_backend-2", BuildFromDir("S:/scala/backend"), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-Yprofile-run-gc", "all")),
    //      TestConfig("00_backend-3", BuildFromDir("S:/scala/backend"), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-Yprofile-run-gc", "all")),
    //      TestConfig("00_backend-4", BuildFromDir("S:/scala/backend"), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-Yprofile-run-gc", "all"))
    //    ),


    //      TestConfig("00_backend-baseline", BuildFromDir("S:/scala/backend-before", false), extraArgs = List("-Yprofile-external-tool", "jvm")),

    //      TestConfig("00_backend-0-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "0")),
    //      TestConfig("00_backend-2-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "2")),
    //      TestConfig("00_backend-4-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4")),
    //      TestConfig("00_backend-8-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8")),
    //      TestConfig("00_backend-12-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "12")),
    //      TestConfig("00_backend-16-mike", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "16"))


    //      TestConfig("00_backend-4-6", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-YmaxQueue", "6")),
    //      TestConfig("00_backend-4-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-YmaxQueue", "8")),
    //      TestConfig("00_backend-4-12", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-YmaxQueue", "12")),
    //      TestConfig("00_backend-4-16", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "4", "-YmaxQueue", "16")),
    //      TestConfig("00_backend-5", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "5", "-Yprofile-run-gc", "all")),
    //      TestConfig("00_backend-6", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-Yprofile-run-gc", "all")),
    //      TestConfig("00_backend-6-6", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-YmaxQueue", "6")),
    //      TestConfig("00_backend-6-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-YmaxQueue", "8")),
    //      TestConfig("00_backend-6-12", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-YmaxQueue", "12")),
    //      TestConfig("00_backend-6-16", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "6", "-YmaxQueue", "16")),
    //      TestConfig("00_backend-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-Yprofile-run-gc", "all"))
    //      TestConfig("00_backend-8-12", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-YmaxQueue", "12")),
    //      TestConfig("00_backend-8-16", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-YmaxQueue", "16")),
    //      TestConfig("00_backend-8-20", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-YmaxQueue", "20")),
    //      TestConfig("00_backend-8-24", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "8", "-YmaxQueue", "24")),
    //      TestConfig("00_backend-10", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "10")),
    //      TestConfig("00_backend-12", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "12")),
    //      TestConfig("00_backend-14", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "14")),
    //      TestConfig("00_backend-16", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "16"))


    //      TestConfig("00_backend-3-A", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "16")),

    //      TestConfig("00_backend-0", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "0")),
    //      TestConfig("00_backend-1-4", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-YmaxQueue", "4")),
    //      TestConfig("00_backend-1-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-YmaxQueue", "8")),
    //      TestConfig("00_backend-1-X", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "1", "-YmaxQueue", "12")),
    //      TestConfig("00_backend-2-4", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-YmaxQueue", "4")),
    //      TestConfig("00_backend-2-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-YmaxQueue", "8")),
    //      TestConfig("00_backend-2-X", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "2", "-YmaxQueue", "12")),
    //      TestConfig("00_backend-3-4", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "4")),
    //      TestConfig("00_backend-3-8", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "8")),
    //      TestConfig("00_backend-3-X", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "12")),
    //      TestConfig("00_backend-3-A", BuildFromDir("S:/scala/backend", false), extraArgs = List("-YmaxAdditionalWriterThreads", "3", "-YmaxQueue", "16"))


  )
}
