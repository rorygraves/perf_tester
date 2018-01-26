serverConnectionType := ConnectionType.Tcp
serverAuthentication := Set(ServerAuthentication.Token)

inThisBuild(Seq(
  organization := "com.chuusai",
  scalaVersion := "2.12.4"
))

incOptions := incOptions.value.withLogRecompileOnMacro(false)

scalacOptions ++= Seq(
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

scalacOptions in console in Compile -= "-Xfatal-warnings"
scalacOptions in console in Test -= "-Xfatal-warnings"

initialCommands in console := """import shapeless._"""

scmInfo :=
  Some(ScmInfo(
    url("https://github.com/milessabin/shapeless"),
    "scm:git:git@github.com:milessabin/shapeless.git"
  ))

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
parallelExecution in Test := false

moduleName := "shapeless"

libraryDependencies ++= Seq(
  "org.typelevel" %% "macro-compat" % "1.1.1",
  scalaOrganization.value % "scala-reflect" % scalaVersion.value % "provided",
  scalaOrganization.value % "scala-compiler" % scalaVersion.value % "provided",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
)
libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
    case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
    // in Scala 2.10, quasiquotes are provided by macro paradise
    case Some((2, 10)) =>
      Seq("org.scalamacros" %% "quasiquotes" % "2.1.1" cross CrossVersion.binary)
  }
}

