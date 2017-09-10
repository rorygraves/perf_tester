#ScalaC Perf tester

A utility that runs multi versions of the scalaC compiler for multiple iterations to provide performance analysis.

To Use - run 

java org.perftester.ProfileMain -s <SCALA_CHECKOUT> -a TEST_BUILD -r RESULTS_DIR --iterations ITERATIONS

Where:
 * SCALA_CHECKOUT is a scalac git checkout  e.g. /workspace/scala
 * TEST_BUILD Test compilation project (probably only works with /workspace/perf_tester/corpus/akka/)
 * RESULTS_DIR - Where to store the run results - /workspace/perf_tester/results/
 * ITERATIONS - The number of iterations to run - recommended > 30
 
 
*n.b* This takes a long time to run - you also, ideally, want to run it on a stable system 
(e.g. not a laptop which slows down when it gets hot)

ScalaC takes some iterations before JIT is stable and results stop getting faster (typically 2+ mins).  Hence the 
need to run (and then ignore) early iterations.

To setup which builds to run and compare, currently this is hardcoded in ```ProfileMain```
`
  // 2.12.1 vs latest
      TestConfig("00_2.12.2", BuildFromGit("21d12e9f5ec1ffe023f509848911476c1552d06f"),extraJVMArgs = List()),
      TestConfig("00_2.12.x", BuildFromGit("e1e8d050deb643ca56db1549e2e5a3114572a952"),extraJVMArgs = List())
`
These are two test configs with names and using git hashes as their baseline. (You can also use a test directory so you 
can test in-development code.)





