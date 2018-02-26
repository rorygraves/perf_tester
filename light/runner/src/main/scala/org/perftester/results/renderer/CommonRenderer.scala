package org.perftester.results.renderer

import org.perftester.results.{PhaseResults, RunResult}

object CommonRenderer {
  def printAggResults(runResult: RunResult, results: Seq[PhaseResults], limit: Double): Unit = {
    val size = (results.size * limit).toInt
    case class Distribution(min: Double, max: Double, mean: Double) {
      def formatPercent(sigDigits: Int, decimalDigits: Int, value: Double): String = {
        String.format(s"%+$sigDigits.${decimalDigits}f", new java.lang.Double(value))
      }

      def formatResult(sigDigits: Int, decimalDigits: Int, value: Double): String = {
        String.format(s"%,$sigDigits.${decimalDigits}f", new java.lang.Double(value))
      }

      def formatted(s: Int, p: Int): String = {
        s"${formatResult(s, p, mean)} [${formatPercent(4, 2, (min / mean) * 100 - 100)}% ${formatPercent(4, 2, (max / mean) * 100 - 100)}%]"
      }
    }
    def distribution(fn: PhaseResults => Double): Distribution = {
      if (results.isEmpty) Distribution(-1, -1, -1)
      else {
        val raw  = (results map fn sorted).take(size)
        val mean = raw.sum / size
        Distribution(raw.head, raw.last, mean)
      }
    }
    val wallClockTimeAvg    = distribution(_.wallClockTimeMS)
    val allWallClockTimeAvg = distribution(_.allWallClockTimeMS)
    val allCpuTimeAvg       = distribution(_.cpuTimeMS)
    val allAllocatedBytes   = distribution(_.allocatedMB)
    val allIdleAvg          = distribution(_.idleTimeMS)

    val wallMsStr            = wallClockTimeAvg.formatted(6, 2)
    val allWallMsStr         = allWallClockTimeAvg.formatted(6, 2)
    val allCpuMsStr          = allCpuTimeAvg.formatted(6, 2)
    val allAllocatedBytesStr = allAllocatedBytes.formatted(6, 2)
    val allIdleMsStr         = allIdleAvg.formatted(6, 2)
    println(
      "%25s\t%4s\t%25s\t%25s\t%25s\t%25s\t%25s"
        .format(runResult.id,
          size,
          wallMsStr,
          allWallMsStr,
          allCpuMsStr,
          allIdleMsStr,
          allAllocatedBytesStr))

  }
}
