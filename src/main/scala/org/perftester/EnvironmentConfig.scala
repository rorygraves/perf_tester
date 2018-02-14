package org.perftester

import ammonite.ops.Path

case class EnvironmentConfig(
                             // the name of the user running the test - used in results directory
                             username: String = "",
                             checkoutDir: Path = Path.home,
                             testDir: Path = Path.home,
                             outputDir: Path = Path.home,
                             iterations: Int = 30,
                             // the name of the test config to run
                             config: String = "",
                             configString: String = null,
                             analyseOnly: Boolean = false,
                             runWithDebug: Boolean = false,
                             overwriteResults: Boolean = true)
