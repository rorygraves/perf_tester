scalaVersion := "2.12.4"
name := "pref-tester-light"
version := "1.0"

Benchmarks.settings

// Here sources to test are defined
Benchmarks.libToTest := "com.typesafe.akka" %% "akka-actor" % "2.5.9"