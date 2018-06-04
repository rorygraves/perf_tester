package org.perftester.renderer

import ammonite.ops.{Path, write}
import org.perftester.{EnvironmentConfig, ProfileMain}
import org.perftester.results.RunDetails

object PhaseRenderer {
  def outputHtmlResults(outputFolder: Path,
                        envConfig: EnvironmentConfig,
                        results: Iterable[RunDetails]): String = {
    val phase = "parser" // make it configurable
    val lines = for (details <- results) yield {
      val name = details.runResult.testConfig.id
      assert(details.runResult.phases.contains(phase))

      val datapoints = details.runResult.rawData.filter(_.phaseName == phase)

      ProfileMain.renderAggResults(details.testId, details.runResult.testConfig, datapoints, 0.1)
    }

    val text = (ProfileMain.aggResultsHeading(s"$phase stats") +: lines.toSeq).mkString("\n")
    val path = outputFolder / s"$phase-stats.txt"
    write.over(path, text)
    text
  }
}
