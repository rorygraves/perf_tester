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

# Intellij integration

Benchmark can be run/debug benchmark in Intellij:
 * create benchmark by running `produceBech` (this is required after any change in Benchmark.scala or build.sbt)
 * It will print path where benchmark was created, e.g. `[success] Benchmark was created in /home/krzysztof/workspace/perf_tester/light/./benchOut/2.12.4`. Note this path.
 * Run Main main class from Intellij (it will fail)
 * Pass location of benchmark (reported in 2nd point) as program argument.


 # How to create benchmark (in `benchOut` dir)
1. Check `benchmark.conf` and put stuff that you want to test
1. run `sbt runner/installScalaCommits` to install scala versions that from config with `buildLocally=true`. This use internal scala repo (created in `scalaRepo` dir).
1. run `runner/generateBenchmarkRunner` to run generate benchmark runner
1. run `+benchmark/produceBench` to generate benchmarks (NOTE: `+` at start is key to make it work!)
1. run all using `benchOut/run.sh` script.