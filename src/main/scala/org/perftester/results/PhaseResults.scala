package org.perftester.results

import org.perftester.results.rows.{BackgroundPhaseRow, GCDataRow, MainPhaseRow, PhaseRow}

case class PhaseResults(main: MainPhaseRow,
                        background: List[BackgroundPhaseRow],
                        gc: List[GCDataRow]) {
  def iterationId: Int = main.runId

  def phaseId: Int = main.phaseId

  def phaseName: String = main.phaseName

  def wallClockTimeMS    = toMs(main.duration)
  def allWallClockTimeMS = toMs(sumAll(_.duration))

  def idleTimeMS: Double = toMs(sumAll(_.idleNs))

  def cpuTimeMS: Double = toMs(sumAll(_.cpuTimeNs))

  def userTimeMS: Double = toMs(sumAll(_.userTimeNs))

  def allocatedMB: Double = sumAll(_.allocatedBytes).toDouble / (1024 * 1024)

  def retainedMB: Double = main.heapSize.toDouble / (1024 * 1024)

  def gcTimeMS: Double =
    gc.map {
      _.duration
    }.sum

  def toMs(ns: Long) = ns / 1000000

  def sumAll(fn: PhaseRow => Long) = fn(main) + (background map fn).sum
}
