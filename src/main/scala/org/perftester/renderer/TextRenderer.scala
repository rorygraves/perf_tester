package org.perftester.renderer

import org.perftester.EnvironmentConfig
import org.perftester.ProfileMain.{printAggResults, calcStats}
import org.perftester.results.rows.MainPhaseRow
import org.perftester.results.{PhaseResults, RunDetails, RunResult}

import scala.collection.mutable

object TextRenderer {
  def apply(envConfig: EnvironmentConfig, results: Iterable[RunDetails]) =
    new TextRenderer(envConfig, results)
}

class TextRenderer(envConfig: EnvironmentConfig, allData: Iterable[RunDetails]) {
  val showGcInfo = !allData.exists(_.runResult.info.version == -1)
  val allStatsPhases: mutable.LinkedHashSet[String] =
    allData.toList.flatMap(_.runResult.phases)(scala.collection.breakOut)
  val phases = allStatsPhases.filter {
    case phase => envConfig.summaryPhases.exists(_.pattern.matcher(phase).matches())
  }

  def outputTextResults: Unit = {
    Console.withOut(getOut(envConfig)) {
      outputTextResultsDetails(false)
      if (envConfig.summaryBaseline) {
        outputTextResultsDetails(true)
      }
    }
  }

  private def getOut(envConfig: EnvironmentConfig) = {
    import java.io.PrintStream
    import java.nio.file.Files
    import java.nio.file.StandardOpenOption._

    envConfig.summaryFile match {
      case None => Console.out
      case Some(file) =>
        Files.createDirectories(file.toNIO)
        new PrintStream(Files.newOutputStream(file.toNIO, WRITE, CREATE, TRUNCATE_EXISTING))
    }
  }

  private def outputTextResultsDetails(summary: Boolean): Unit = {
    val results = if (summary) {
      (this.allData groupBy (_.runResult.testConfig.id) map {
        case (id, r) =>
          r.reduce[RunDetails] {
            case (r1, r2) =>
              r1.copy(cycleNumber = -1,
                      runResult =
                        r1.runResult.copy(rawData = r1.runResult.rawData ++ r2.runResult.rawData))
          }
      }).toList.sorted
    } else this.allData
    heading("ALL", summary)
    val baselineId    = results.head.runResult.testConfig.id
    val baselineStats = calcStats(results.head.runResult.rawData, 1)
    results.foreach {
      case RunDetails(cycleId, testId, RunResult(testConfig, info, rawData, iteration, phases)) =>
        printAggResults(cycleId,
                        testConfig,
                        summary,
                        calcStats(rawData, 1),
                        baselineStats,
                        baselineId)
    }

    if (envConfig.iterations > 10) {
      val range = envConfig.summaryPercent
      for (i      <- List(0); //<- 10 until (envConfig.iterations, 10);
           bestPC <- range) {
        println(
          "\n---------------------------------------------------------------------------------------------------")
        println(
          "---------------------------------------------------------------------------------------------------")
        heading(s"after $i best $bestPC%", summary)
        val baselineStats =
          calcStats(results.head.runResult.rawData.dropWhile(_.iterationId <= i), bestPC / 100.0)
        results.foreach {
          case RunDetails(cycleId,
                          testId,
                          RunResult(testConfig, info, rawData, iteration, phases)) =>
            val skipped = rawData.dropWhile(_.iterationId <= i)
            printAggResults(cycleId,
                            testConfig,
                            summary,
                            calcStats(allPhases(skipped), bestPC / 100.0),
                            baselineStats,
                            baselineId)
        }

        for (phase <- phases) {
          heading(s"after $i best $bestPC%, phase $phase", summary)
          val baselineStats = calcStats(results.head.runResult.rawData.filter {
            case row => row.iterationId > i && row.phaseName == phase
          }, bestPC / 100.0)
          for {
            RunDetails(cycleId, testId, RunResult(testConfig, info, rawData, iteration, phases)) <- results
          } {
            val skipped = rawData.filter {
              case row => row.iterationId > i && row.phaseName == phase
            }

            printAggResults(cycleId,
                            testConfig,
                            summary,
                            calcStats(skipped, bestPC / 100.0),
                            baselineStats,
                            baselineId)
          }
        }
        if (showGcInfo)
          for (phase <- phases) {
            heading(s"after $i best $bestPC%, phase $phase no GC", summary)
            val baselineStats = calcStats(results.head.runResult.rawData.filter {
              case row => row.iterationId > i && row.phaseName == phase && row.gcTimeMS == 0
            }, bestPC / 100.0)
            for {
              RunDetails(cycleId, testId, RunResult(testConfig, info, rawData, iteration, phases)) <- results
            } {
              val skipped = rawData.filter {
                case row => row.iterationId > i && row.phaseName == phase && row.gcTimeMS == 0
              }

              printAggResults(cycleId,
                              testConfig,
                              summary,
                              calcStats(skipped, bestPC / 100.0),
                              baselineStats,
                              baselineId)
            }
          }
      }
    }
  }

  private def heading(title: String, summary: Boolean) {
    println(
      f"-----\n$title${if (summary) " - summary" else ""}\n${"Run Name"}%25s${""}%40s${if (summary) ""
      else "\tCycle"}\tsamples\t${"Wall time (ms)"}%25s\t${"All Wall time (ms)"}%25s\t${"CPU(ms)"}%25s\t${"Idle time (ms)"}%25s\t${"Allocated(MBs)"}%25s")
  }

  private def allPhases(raw: Seq[PhaseResults]): Seq[PhaseResults] = {
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
}
