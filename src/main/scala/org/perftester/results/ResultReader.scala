package org.perftester.results

import ammonite.ops.{Path, read}
import org.perftester.TestConfig

import scala.collection.SortedSet

object ResultReader {
  private val totalOrSingle = Set(ResultType.SINGLE.toString,ResultType.TOTAL.toString)
  private val total= Set(ResultType.TOTAL.toString)
  private val single = Set(ResultType.SINGLE.toString)
  def readResults(testConfig: TestConfig, file : Path, iterations:Int): RunResult = {
    val lines = read.lines! file
    val asValues = lines.map(_.split(',').toList)
    val dataLines = asValues.filter( value =>
      value(0) == "data" && total(value(4)) )
 //     value(0) == "data" && value(4) ==  ResultType.SINGLE)
    val rows = dataLines.map { row =>
      PhaseResults(
        // data,
        row(1).toInt, // iteration id
        row(2).toInt, // phaseId
        row(3), // phaseName
        ResultType.withName(row(4)), // type
        row(5).toInt, // id
        row(6), // comment
        row(7).toDouble, // wallClockTimeMs,
        row(8).toDouble, // idleTimeMs,
        row(9).toDouble, // cpuTimeMs,
        row(10).toDouble, // userTimeMS
        row(11).toDouble, // allocatedMB
        row(12).toDouble, // retainedMB
        row(13).toDouble// gcTimeMs

      )
    }
    val alIterations = SortedSet((1 to iterations).toList :_*)
    val phases = rows.groupBy(_.phaseName).keySet
    RunResult(testConfig,rows, alIterations, phases)
  }


}
