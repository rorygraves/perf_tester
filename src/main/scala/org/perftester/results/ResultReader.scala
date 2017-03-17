package org.perftester.results

import ammonite.ops.{Path, read}
import org.perftester.TestConfig

object ResultReader {
  def readResults(testConfig: TestConfig, iteration: Int, file : Path): RunResult = {
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
        row(8).toDouble// gcTimeMs

      )
    }
    RunResult(testConfig,iteration,SingleExecutionResult(rows.toList))
  }


}
