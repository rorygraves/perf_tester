package org.perftester.results

case class PhaseResults(iterationId: Int, phaseId: Int,
                        phaseName: String, wallClockTimeMS : Double, cpuTimeMS : Double,
                        userTimeMS : Double, allocatedMB: Double, retainedMB: Double, gcTimeMS: Double)
