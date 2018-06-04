package org.perftester.renderer

import org.perftester.ProfileMain.printAggResults
import org.perftester.{EnvironmentConfig, ProfileMain, TestConfig}
import org.perftester.results.{PhaseResults, RunDetails, RunResult}
import org.perftester.results.rows.MainPhaseRow

import scala.collection.mutable

object TextRenderer {

  def outputTextResults(envConfig: EnvironmentConfig, results: Iterable[RunDetails]): Unit = {

    def heading(title: String) = println(ProfileMain.aggResultsHeading(title))

    def allPhases(raw: Seq[PhaseResults]): Seq[PhaseResults] = {
      val res = raw.groupBy(_.iterationId) map {
        case (iterationNo, iterationData) =>
          val first   = iterationData.head
          val startNs = iterationData.sortBy(_.main.startNs).head.main.startNs
          val endNs   = iterationData.sortBy(-_.main.endNs).head.main.endNs

          val mainPhaseRow: MainPhaseRow = MainPhaseRow(
            startNs = startNs,
            endNs = endNs,
            runId = first.main.runId,
            phaseId = -1,
            phaseName = "all",
            purpose = "collated",
            taskCount = -1,
            threadId = -1,
            threadName = "N/A",
            runNs = endNs - startNs,
            idleNs = iterationData.map(_.main.idleNs).sum,
            cpuTimeNs = iterationData.map(_.main.cpuTimeNs).sum,
            userTimeNs = iterationData.map(_.main.userTimeNs).sum,
            allocatedBytes = iterationData.map(_.main.allocatedBytes).sum,
            heapSize = iterationData.map(_.main.heapSize).max
          )
          PhaseResults(
            main = mainPhaseRow,
            background = iterationData.flatMap(_.background)(scala.collection.breakOut),
            gc = iterationData.flatMap(_.gc)(scala.collection.breakOut)
          )

      }
      res.toSeq
    }

    heading("ALL")
    results.foreach {
      case RunDetails(cycleId, testId, RunResult(testConfig, rawData, iteration, phases)) =>
        printAggResults(cycleId, testConfig, allPhases(rawData), 1)
    }
    val phases: mutable.LinkedHashSet[String] =
      results.toList.flatMap(_.runResult.phases)(scala.collection.breakOut)

    if (envConfig.iterations > 10) {
      for (i      <- List(0); //<- 10 until (envConfig.iterations, 10);
           bestPC <- (100 until (0, -5)).toList ::: List(1)) {
        println(
          "\n---------------------------------------------------------------------------------------------------")
        println(
          "---------------------------------------------------------------------------------------------------")
        heading(s"after $i best $bestPC%")
        results.foreach {
          case RunDetails(cycleId, testId, RunResult(testConfig, rawData, iteration, phases)) =>
            val skipped = rawData.dropWhile(_.iterationId <= i)
            printAggResults(cycleId, testConfig, allPhases(skipped), bestPC / 100.0)
        }

        for (phase <- phases) {
          heading(s"after $i best $bestPC%, phase $phase")
          for {
            RunDetails(cycleId, testId, RunResult(testConfig, rawData, iteration, phases)) <- results
          } {
            val skipped = rawData.filter {
              case row => row.iterationId > i && row.phaseName == phase
            }

            printAggResults(cycleId, testConfig, skipped, bestPC / 100.0)
          }
        }
        for (phase <- phases) {
          heading(s"after $i best $bestPC%, phase $phase no GC")
          for {
            RunDetails(cycleId, testId, RunResult(testConfig, rawData, iteration, phases)) <- results
          } {
            val skipped = rawData.filter {
              case row => row.iterationId > i && row.phaseName == phase && row.gcTimeMS == 0
            }

            printAggResults(cycleId, testConfig, skipped, bestPC / 100.0)
          }
        }
      }
    }
  }
}
