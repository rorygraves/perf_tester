name := "perf_tester"

version := "0.1"
scalaVersion := "2.12.8"

lazy val scala213               = "2.13.0-RC1"
lazy val scala212               = "2.12.8"
lazy val supportedScalaVersions = List(scala212, scala213)

resolvers += Resolver.bintrayRepo("dhpcs", "maven")

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.3")

mainClass in assembly := Some("org.perftester.ProfileMain")

// scalafmt configuration
scalafmtOnCompile in ThisBuild := true     // all projects
scalafmtTestOnCompile in ThisBuild := true // all projects

lazy val ChildMain = (project in file("src/ChildMain"))
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.12.8"
  )
  .dependsOn(SharedComms)

lazy val SharedComms = (project in file("src/SharedComms"))
  .settings(
    // set to exactly one Scala version
    crossScalaVersions := List(scala212),
    crossPaths := false,
    autoScalaLibrary := false
  )
lazy val PerfTester = (project in file("src/PerfTester"))
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-math"     % "2.2",
    libraryDependencies += "com.github.scopt"   %% "scopt"           % "3.5.0",
    libraryDependencies += "com.typesafe.akka"  %% "akka-actor"      % "2.5.6",
    libraryDependencies += "com.typesafe.akka"  %% "akka-testkit"    % "2.5.6",
    libraryDependencies += "com.typesafe.akka"  %% "akka-slf4j"      % "2.5.6",
    libraryDependencies += "org.apache.commons" % "commons-lang3"    % "3.4",
    libraryDependencies += "com.lihaoyi"        %% "ammonite-ops"    % "1.0.2",
    libraryDependencies += "com.lihaoyi"        %% "scalatags"       % "0.6.7",
    libraryDependencies += "ch.qos.logback"     % "logback-classic"  % "1.2.3",
    libraryDependencies += "org.eclipse.jgit"   % "org.eclipse.jgit" % "4.11.0.201803080745-r",
    libraryDependencies += "com.dhpcs"          %% "scala-json-rpc"  % "2.0.1",
    libraryDependencies += "org.scalactic"      %% "scalactic"       % "3.0.4",
    libraryDependencies += "org.scalatest"      %% "scalatest"       % "3.0.4" % "test",
    libraryDependencies += "junit"              % "junit"            % "4.11" % "test",
    crossScalaVersions := List(scala212)
  )
  .dependsOn(SharedComms)
