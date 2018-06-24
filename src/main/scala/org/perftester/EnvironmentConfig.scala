package org.perftester

import ammonite.ops.Path

import scala.util.matching.Regex

case class EnvironmentConfig(
                             // the name of the user running the test - used in results directory
                             username: String = "",
                             checkoutDir: Path = Path.home,
                             testDir: Path = Path.home,
                             outputDir: Path = Path.home,
                             scalacBuildCache: Path = Path.home / "scalacBuildCache",
                             iterations: Int = 30,
                             processes: Int = 1,
                             // the name of the test config to run
                             config: String = "",
                             configString: String = null,
                             analyseOnly: Boolean = false,
                             runWithDebug: Boolean = false,
                             overwriteResults: Boolean = true,
                             summaryFile: Option[Path] = None,
                             summaryPhases: List[Regex] = List(".*".r),
                             summaryPercent: List[Int] =
                               ((100 until (0, -5)).toList ::: List(1)).distinct.sorted,
                             summaryBaseline: Boolean = false)
