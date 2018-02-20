# Experimental light tester

Consist of sbt project with benchmarking engine and sbt task to create benchmark instance.

Benchmark instance is a dir with defined layout that consist of:
 - sources to compile in `sources` dir
 - benchmark engine code in `bench.jar`
 - run script in `run.sh`
 - classpath of compilation in `cpJars` dir
 - scala jars in `scalaJars` dir

Benchmark is generated for library specified in `build.sbt` in `TesterOutput.libToTest` setting.
Using `TesterOutput.libToTest := "com.typesafe.akka" %% "akka-actor" % "2.5.9"` will test compilation of akka actor.
Benchmark will use specified version of scala (in standard sbt setting `scalaVersion`)

Sbt build will resolve source jar (that will be unzipped to sources directory) and compile time dependencies (using ivy).