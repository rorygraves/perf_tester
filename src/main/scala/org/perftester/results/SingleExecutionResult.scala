package org.perftester.results

case class SingleExecutionResult(phases: List[PhaseResults]) {
  def phaseAllocatedBytes(phaseId: Int): Double = byPhaseId(phaseId).allocatedMB

  def phaseWallClockMS(phaseId: Int): Double = byPhaseId(phaseId).wallClockTimeMS

  def phaseCPUMS(phaseId: Int): Double = byPhaseId(phaseId).cpuTimeMS

  val byPhaseId: Map[Int, PhaseResults] = phases.map(p => (p.phaseId, p)).toMap
  def allWallClockMS: Double = phases.map(_.wallClockTimeMS).sum
  def allAllocated: Double = phases.map(_.allocatedMB).sum
  def allCPUTime: Double = phases.map(_.cpuTimeMS).sum

}
