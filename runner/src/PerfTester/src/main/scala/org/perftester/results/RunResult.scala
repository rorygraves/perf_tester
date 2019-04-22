package org.perftester.results

import org.perftester.TestConfig
import org.perftester.results.rows.InfoRow

import scala.collection.SortedSet

case class RunResult(testConfig: TestConfig,
                     info: InfoRow,
                     rawData: Seq[PhaseResults],
                     iterations: SortedSet[Int],
                     phases: Set[String]) {

  rawData foreach { r =>
    require(phases(r.phaseName), r.phaseName)
  }
}
case class RunDetails(cycleNumber: Int, testId: Int, runResult: RunResult)
    extends Ordered[RunDetails] {
  override def compare(that: RunDetails): Int = {
    val major = this.testId compareTo that.testId
    if (major != 0) major else this.cycleNumber compareTo that.cycleNumber
  }
}
