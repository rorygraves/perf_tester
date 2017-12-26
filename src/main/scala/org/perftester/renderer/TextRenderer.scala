package org.perftester.renderer

import org.perftester.ProfileMain.printAggResults
import org.perftester.{EnvironmentConfig, TestConfig}
import org.perftester.results.RunResult

import scala.collection.mutable

object TextRenderer {
  def outputTextResults(envConfig: EnvironmentConfig, results: Seq[(TestConfig, RunResult)]): Unit = {
    def heading(title: String) {
      println(f"-----\n$title\n${"RunName"}%25s\t${"AllWallMS"}%25s\t${"CPU_MS"}%25s\t${"Allocated"}%25s")
    }

    heading("ALL")
    results.foreach { case (config, configResult) =>
      printAggResults(config, configResult.all)
    }

    if(envConfig.iterations > 10) {
      (10 until(envConfig.iterations, 10)) foreach { i =>
        println("\n---------------------------------------------------------------------------------------------------")
        println("---------------------------------------------------------------------------------------------------")
        heading(s"after $i 90%")
        results.foreach { case (config, configResult) =>
          printAggResults(config, configResult.filterIteration(i, 10000).std)
        }

        val phases: mutable.LinkedHashSet[String] = results.flatMap(r => r._2.phases)(scala.collection.breakOut)

        for (phase <- phases) {
          heading(s"after $i 90%, phase $phase")
          for {(config, configResult) <- results} {
            printAggResults(config, configResult.filterIteration(i, 10000).filterPhases(phase).std)
          }
        }
      }
    }
  }
}
