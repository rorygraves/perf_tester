package org.perftester.results

import ammonite.ops.{Path, read}
import org.perftester.TestConfig

import scala.collection.SortedSet

object ResultReader {
  def readResults(testConfig: TestConfig, file : Path, iterations:Int): RunResult = {
    val lines = read.lines! file
    val asValues = lines.map(_.split(',').toList)
    val dataLines = asValues.filter(_.head == "data")
    val rows = dataLines.map { row =>
      PhaseResults(
        // data,
        row(1).toInt, // iteration id
        row(2).toInt, // phaseId
        row(3), // phaseName
        row(4).toDouble, // wallClockTimeMs,
        row(5).toDouble, // cpuTimeMs,
        row(6).toDouble, // userTimeMS
        row(7).toDouble, // allocatedMB
        row(8).toDouble, // retainedMB
        row(9).toDouble// gcTimeMs

      )
    }
    val alIterations = SortedSet((1 to iterations).toList :_*)
    val phases = rows.groupBy(_.phaseName).keySet
    RunResult(testConfig,rows, alIterations, phases)
  }


}
