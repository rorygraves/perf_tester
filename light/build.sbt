import sbt.Keys.name

version.in(ThisBuild) := "1.0"

val runner = project.settings(Runner.settings).settings(libraryDependencies += "com.typesafe" % "config" % "1.3.2")

val benchmark = project.settings(
  scalaVersion := "2.12.4",
  name := "pref-tester-light",
  Benchmarks.libToTest := "com.typesafe.akka" % "akka-actor_2.12" % "2.5.9",
  crossScalaVersions := Runner.benchmarks.in(runner).value.values.map(_.scalaVersion).toList.distinct
).settings(Benchmarks.settings)




// Here sources to test are defined
