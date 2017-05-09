package org.perftester.results

import java.util

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
  class Distribution(results : Array[Double]) {
    val mean = results.sum/size
    def size = results.length
    def median = at(.5)
    def iqr = at(.75) - at(.25)
    def at(pos:Double) = {
      assert (pos >= 0.0)
      assert (pos <= 1.0)
      val index= ((size -1) * pos).toInt
      results(index)
    }
    def atPC(pos:Double) = {
      at(pos) / mean
    }
    override def toString: String = s"$mean [+${atPC(1)}% :${atPC(.9)}% -${atPC(0)}%"
    private def format(s:Int,p:Int, v:Double) :String = {
      String.format(s"%$s.${p}f", new java.lang.Double(v))
    }
    def formatted(s:Int,p:Int) = {
      s"${format(s, p, mean)} [+${format(4, 2, atPC(1))}% -${format(4, 2, atPC(0))}%]"
    }
  }
  object Distribution {
    def range(lower:Double, upper:Double, aggregate: Aggregate, fn : (PhaseResults) => Double) = {
      val results : Array[Double] = aggregate.grouped.map(fn)(scala.collection.breakOut)
      util.Arrays.sort(results)
      new Distribution(results.slice((results.size *lower).toInt, (results.size *upper).toInt))
    }
  }

  lazy val byPhaseId = rawData.groupBy(_.phaseId) map {case (k,v) => k -> new Aggregate(v) }
  lazy val byPhaseName = rawData.groupBy(_.phaseName) map {case (k,v) => k -> new Aggregate(v) }
  lazy val byIteration = rawData.groupBy(_.iterationId) map {case (k,v) => k -> new Aggregate(v) }


  lazy val totals = {
    val byIteration = rawData.groupBy(_.iterationId) map {case (k,v) => k -> new Aggregate(v).totals }
    new Aggregate(byIteration.values.toList)
  }

  class Detail (lower:Double, upper:Double) {

    def phaseAllocatedBytes(phase: String) = Distribution.range(lower, upper, byPhaseName(phase), _.allocatedMB)

    def phaseWallClockMS(phase: String) = Distribution.range(lower, upper, byPhaseName(phase), _.wallClockTimeMS)

    def phaseCpuMS(phase: String) = Distribution.range(lower, upper, byPhaseName(phase), _.cpuTimeMS)

    lazy val allWallClockMS = Distribution.range(lower, upper, totals, _.wallClockTimeMS)
    lazy val allAllocated = Distribution.range(lower, upper, totals, _.allocatedMB)
    lazy val allCPUTime = Distribution.range(lower, upper, totals, _.cpuTimeMS)
  }
  val all = new Detail(0,1)
  val std = new Detail(0,.9)
}
