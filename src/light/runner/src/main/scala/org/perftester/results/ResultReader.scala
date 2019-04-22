package org.perftester.results

import java.nio.file.{Files, Path}

import org.perftester.results.rows._

import scala.collection.SortedSet
import collection.JavaConverters._

object ResultReader {
  def readResults(id: String, file: Path, iterations: Int): RunResult = {
    val lines    = Files.readAllLines(file).asScala
    val asValues = lines.map(_.split(',').map(_.trim).toList).toList
    val info = asValues.collectFirst {
      case values if values(0) == "info" => InfoRow.parse(values)
    }.get
    val dataRows = asValues.flatMap(DataRow(_, info.version))

    val gcInfo = dataRows.collect {
      case gc: GCDataRow => gc
    }

    def gcEvents(start: Long, end: Long) = gcInfo.collect {
      case gc @ GCDataRow(startNs, endNs, _, _, _, _, _, _)
          if (startNs > start && startNs < end) || (endNs > start && endNs < end) =>
        gc
    }

    val background = dataRows
      .collect {
        case row: BackgroundPhaseRow => row
      }
      .groupBy { row: BackgroundPhaseRow =>
        (row.runId, row.phaseName)
      }
      .withDefaultValue(Nil)

    val rows = dataRows
      .collect {
        case row: MainPhaseRow =>
          PhaseResults(row,
                       background((row.runId, row.phaseName)),
                       gcEvents(row.startNs, row.endNs))
      }
      .sortBy(r => (r.iterationId, r.phaseId))
    val allPhases     = rows.groupBy(_.phaseName).keySet

    RunResult(id, rows, allPhases)
  }
}
