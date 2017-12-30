package org.perftester.results

import ammonite.ops.{Path, read}
import org.perftester.TestConfig
import org.perftester.results.rows.{DataRow, MainDataRowType, PhaseRow}

import scala.collection.SortedSet

object ResultReader {
  def readResults(testConfig: TestConfig, file: Path, iterations: Int): RunResult = {
    val lines    = read.lines ! file
    val asValues = lines.map(_.split(',').toList)
    val dataRows = asValues.flatMap(DataRow(_))

    val rows = dataRows.filter(_.rowType == MainDataRowType).map {
      case row: PhaseRow =>
        PhaseResults(
          // data,
          row.runId, // iteration id
          row.phaseId, // phaseId
          row.phaseName, // phaseName
          ResultType.MAIN, // type ??????????????????
          row.runId, // id
          row.purpose, // comment
          row.runMs.toDouble, // wallClockTimeMs,
          row.idleMs.toDouble, // idleTimeMs,
          row.cpuTimeMs.toDouble, // cpuTimeMs,
          row.userTimeMs, // userTimeMS
          row.allocatedMbs, // allocatedMB
          row.heapSizeMb, // retainedMB
          0.toDouble // gcTimeMs
        )
    }

    val alIterations = SortedSet((1 to iterations).toList: _*)
    val phases       = rows.groupBy(_.phaseName).keySet
    RunResult(testConfig, rows, alIterations, phases)
  }
}
