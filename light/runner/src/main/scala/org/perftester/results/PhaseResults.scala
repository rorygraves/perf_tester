package org.perftester.results

import org.perftester.results.rows.{BackgroundPhaseRow, GCDataRow, MainPhaseRow, PhaseRow}

//object ResultType extends Enumeration {
//  //single threaded
//  val SINGLE = Value("single")
//  //seperate task
//  val TASK = Value("task")
//  //the main thread of a MT
//  val MAIN = Value("main")
//  //total of MT
//  val TOTAL = Value("total")
//  //all of the runs
//  val ALL = Value("all")
//  //used in merge
//  val NA = Value("--")
//
//}

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

//object PhaseResults {
//  val empty =
//    PhaseResults(MainPhaseRow(-1, -1, -1, -1, "empty", null, -1, "empty", 0, 0, 0, 0, 0, 0),
//                 Nil,
//                 Nil)
//
//  def union(results: Seq[PhaseResults], fn: (Long, Long) => Long): PhaseResults = {
//    results reduce { (p1, p2) =>
//      val main = MainPhaseRow()
//      PhaseResults(
//        -1,
//        -1,
//        "",
//        ResultType.NA,
//        -1,
//        "",
//        wallClockTimeMS = fn(p1.wallClockTimeMS, p2.wallClockTimeMS),
//        idleTimeMS = fn(p1.idleTimeMS, p2.idleTimeMS),
//        cpuTimeMS = fn(p1.cpuTimeMS, p2.cpuTimeMS),
//        userTimeMS = fn(p1.userTimeMS, p2.userTimeMS),
//        allocatedMB = fn(p1.allocatedMB, p2.allocatedMB),
//        retainedMB = fn(p1.retainedMB, p2.retainedMB),
//        gcTimeMS = fn(p1.gcTimeMS, p2.gcTimeMS)
//      )
//    }
//  }
//
//  def transform(results: PhaseResults, fn: (Double) => Double): PhaseResults = {
//    PhaseResults(
//      results.iterationId,
//      results.phaseId,
//      results.phaseName,
//      results.resultType,
//      results.id,
//      results.comment,
//      wallClockTimeMS = fn(results.wallClockTimeMS),
//      idleTimeMS = fn(results.idleTimeMS),
//      cpuTimeMS = fn(results.cpuTimeMS),
//      userTimeMS = fn(results.userTimeMS),
//      allocatedMB = fn(results.allocatedMB),
//      retainedMB = fn(results.retainedMB),
//      gcTimeMS = fn(results.gcTimeMS)
//    )
//  }
//}
