package org.perftester.results

object ResultType extends Enumeration {
  //single threaded
  val SINGLE= Value("single")
  //seperate task
  val TASK= Value("task")
  //the main thread of a MT
  val MAIN= Value("main")
  //total of MT
  val TOTAL= Value("total")
  //all of the runs
  val ALL= Value("all")
  //used in merge
  val NA= Value("--")

}
case class PhaseResults private (iterationId: Int, phaseId: Int,
                        phaseName: String, resultType:ResultType.Value, id:Int, comment:String,
                        wallClockTimeMS : Double, idleTimeMS : Double, cpuTimeMS : Double,
                        userTimeMS : Double, allocatedMB: Double, retainedMB: Double, gcTimeMS: Double)


object PhaseResults {

  def combine(results: Seq[PhaseResults], fn: (Double, Double) => Double): PhaseResults = {
    results reduce {
      (p1, p2) =>
        PhaseResults(-1, -1, "", ResultType.NA, -1, "",
          wallClockTimeMS = fn(p1.wallClockTimeMS, p2.wallClockTimeMS),
          idleTimeMS = fn(p1.idleTimeMS, p2.idleTimeMS),
          cpuTimeMS = fn(p1.cpuTimeMS, p2.cpuTimeMS),
          userTimeMS = fn(p1.userTimeMS, p2.userTimeMS),
          allocatedMB = fn(p1.allocatedMB, p2.allocatedMB),
          retainedMB = fn(p1.retainedMB, p2.retainedMB),
          gcTimeMS = fn(p1.gcTimeMS, p2.gcTimeMS))
    }
  }

  def transform(results: PhaseResults, fn: (Double) => Double): PhaseResults = {
    PhaseResults(results.iterationId,results.phaseId,results.phaseName,results.resultType,
      results.id, results.comment,
      wallClockTimeMS = fn(results.wallClockTimeMS),
      idleTimeMS = fn(results.idleTimeMS),
      cpuTimeMS = fn(results.cpuTimeMS),
      userTimeMS = fn(results.userTimeMS),
      allocatedMB = fn(results.allocatedMB),
      retainedMB = fn(results.retainedMB),
      gcTimeMS = fn(results.gcTimeMS))
  }
}

