name := "perf_tester"

version := "0.1"

scalaVersion := "2.12.6"

resolvers += Resolver.bintrayRepo("dhpcs", "maven")

libraryDependencies += "com.dhpcs" %% "scala-json-rpc" % "2.0.1"

libraryDependencies += "org.apache.commons" % "commons-math"     % "2.2"
libraryDependencies += "com.github.scopt"   %% "scopt"           % "3.5.0"
libraryDependencies += "com.typesafe.akka"  %% "akka-actor"      % "2.5.6"
libraryDependencies += "com.typesafe.akka"  %% "akka-testkit"    % "2.5.6"
libraryDependencies += "com.typesafe.akka"  %% "akka-slf4j"      % "2.5.6"
libraryDependencies += "org.apache.commons" % "commons-lang3"    % "3.4"
libraryDependencies += "com.lihaoyi"        %% "ammonite-ops"    % "1.0.2"
libraryDependencies += "com.lihaoyi"        %% "scalatags"       % "0.6.7"
libraryDependencies += "ch.qos.logback"     % "logback-classic"  % "1.2.3"
libraryDependencies += "org.eclipse.jgit"   % "org.eclipse.jgit" % "4.11.0.201803080745-r"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

libraryDependencies += "junit" % "junit" % "4.11" % "test"
//scala compiler only for direct compilation
//should be a seperate project - just needed for the launcher
libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.12.6"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.3")

mainClass in assembly := Some("org.perftester.ProfileMain")

// scalafmt configuration
scalafmtOnCompile in ThisBuild := true     // all projects
scalafmtTestOnCompile in ThisBuild := true // all projects

TaskKey[Unit]("exitSBT") := ???
