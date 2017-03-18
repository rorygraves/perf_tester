package org.perftester.results

import java.text.DecimalFormat

import org.perftester.TestConfig

import scala.collection.SortedSet

case class RunResult (testConfig: TestConfig, rawData : Seq[PhaseResults], iterations: SortedSet[Int], phases : Set[String]) {

  rawData foreach {r => require (iterations(r.iterationId))}

  rawData foreach {r => require (phases(r.phaseName))}

  def filterIteration(min:Int, max:Int) = {
    val filter = (min to max).toSet
    new RunResult(testConfig, rawData.filter(res => filter(res.iterationId)), iterations.intersect(filter), phases)
  }
  def filterPhases(phaseNames:String*) = {
    val filter = phaseNames.toSet
    new RunResult(testConfig, rawData.filter(res => filter(res.phaseName)), iterations, phases.intersect(filter))
  }
  def filterNoGc = new RunResult(testConfig,
    rawData.filter{res =>
    res.gcTimeMS == 0},
    iterations, phases
  )

  class Aggregate(val grouped : Seq[PhaseResults]) {
    val totals = PhaseResults.combine(grouped, (_ + _))
    val min = PhaseResults.combine(grouped, Math.min(_ , _))
    val max = PhaseResults.combine(grouped, Math.max(_, _))
    val mean = PhaseResults.transform(totals, ( _ / grouped.size))
  }
  class Distribution(min:Double, mean:Double, max:Double) {
    val neg = ((mean - min) /mean * 1000).toInt/10.0
    val pos = ((max - mean) /mean * 1000).toInt/10.0
    override def toString: String = s"$mean [+$pos% -$neg%"
    java.lang.Double.toString(mean)
    def formatted(s:Int,p:Int) = {
      val d= String.format(s"%$s.${p}f", new java.lang.Double(mean))
      s"$d [+${pos}% -${neg}%]"
    }
  }
  object Distribution {
    def apply (aggregate: Aggregate, fn : (PhaseResults) => Double) = {
      new Distribution(fn(aggregate.min), fn(aggregate.mean), fn(aggregate.max))
    }
  }

  lazy val byPhaseId = rawData.groupBy(_.phaseId) map {case (k,v) => (k -> new Aggregate(v)) }
  lazy val byPhaseName = rawData.groupBy(_.phaseName) map {case (k,v) => (k -> new Aggregate(v)) }
  lazy val byIteration = rawData.groupBy(_.iterationId) map {case (k,v) => (k -> new Aggregate(v)) }


  lazy val totals = {
    val byIteration = rawData.groupBy(_.iterationId) map {case (k,v) => (k -> new Aggregate(v).totals) }
    new Aggregate(byIteration.values.toList)
  }


  def phaseAllocatedBytes(phase: String)= Distribution(byPhaseName(phase), _.allocatedMB)
  def phaseWallClockMS(phase: String)= Distribution(byPhaseName(phase), _.wallClockTimeMS)
  def phaseCpuMS(phase: String)= Distribution(byPhaseName(phase), _.cpuTimeMS)

  lazy val allWallClockMS= Distribution(totals, _.wallClockTimeMS)
  lazy val allAllocated= Distribution(totals, _.allocatedMB)
  lazy val allCPUTime= Distribution(totals, _.cpuTimeMS)

}
