package org.perftester.results

case class PhaseResults(iterationId: Int, phaseId: Int,
                        phaseName: String, wallClockTimeMS : Double, cpuTimeMS : Double,
                        userTimeMS : Double, allocatedMB: Double, retainedMB: Double, gcTimeMS: Double)


object PhaseResults {
  def combine(results: Seq[PhaseResults], fn: (Double, Double) => Double): PhaseResults = {
    results reduce {
      (p1, p2) =>
        PhaseResults(-1, -1, "",
          wallClockTimeMS = fn(p1.wallClockTimeMS, p2.wallClockTimeMS),
          cpuTimeMS = fn(p1.cpuTimeMS, p2.cpuTimeMS),
          userTimeMS = fn(p1.userTimeMS, p2.userTimeMS),
          allocatedMB = fn(p1.allocatedMB, p2.allocatedMB),
          retainedMB = fn(p1.retainedMB, p2.retainedMB),
          gcTimeMS = fn(p1.gcTimeMS, p2.gcTimeMS))
    }
  }

  def transform(results: PhaseResults, fn: (Double) => Double): PhaseResults = {
    PhaseResults(-1, -1, "",
      wallClockTimeMS = fn(results.wallClockTimeMS),
      cpuTimeMS = fn(results.cpuTimeMS),
      userTimeMS = fn(results.userTimeMS),
      allocatedMB = fn(results.allocatedMB),
      retainedMB = fn(results.retainedMB),
      gcTimeMS = fn(results.gcTimeMS))
  }
}

