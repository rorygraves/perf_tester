package benchmarks

import java.nio.file.{Files, Path, Paths}

object Main extends App {
  val startTime = System.currentTimeMillis()
  val rootPath: Path = Paths.get(args.headOption.getOrElse("."))
  val compilerSetup = new CompilerSetup(rootPath, args.drop(3).toList)
  val N = args.drop(1).headOption.map(_.toInt).getOrElse(2) // TODO change it!
	val M = args.drop(2).headOption.map(_.toInt).getOrElse(15)

	val sources = IO.listSourcesIn(rootPath.resolve("sources")).map(_.toString)

  def runCompilation(n: Int): Long = {
    val run = new compilerSetup.global.Run
    val start = System.currentTimeMillis()
    run.compile(sources)
    val duration = System.currentTimeMillis() - start
    Files.move(compilerSetup.currentOutput, compilerSetup.currentOutput.resolveSibling(s"classes_$n"))
    duration
  }

  val times = (1 to N).map(runCompilation)
  val total = System.currentTimeMillis() - startTime

  def asSec(long: Long) = long  / 1000.0
  def asSec(d: Double) = d  / 1000

  val overhead = asSec(total - times.sum)
  val lastMAvg = asSec(times.takeRight(M).sum / M.toDouble)
  val allAvg = asSec(times.sum / N.toDouble)

  // TODO proper output format
  println(s"Run $N compilations in ${asSec(total)} with overhead: $overhead.")
  println(s"Avgs. Last ($M): $lastMAvg, all $allAvg")
  println(s"Times: ${times.map(asSec)}")
}