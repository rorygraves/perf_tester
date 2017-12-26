name := "perf_tester"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies += "org.apache.commons" % "commons-math" % "2.2"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.6"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.6"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.5.6"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4"
libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "1.0.2"
libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.6.7"
libraryDependencies += "com.dhpcs" %% "scala-json-rpc" % "2.0.1"

mainClass in assembly := Some("org.perftester.ProfileMain")