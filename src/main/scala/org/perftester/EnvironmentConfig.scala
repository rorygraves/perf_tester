package org.perftester

import ammonite.ops.Path

case class EnvironmentConfig(
  checkoutDir: Path = Path.home,
  testDir: Path= Path.home,
  outputDir: Path= Path.home,
  iterations: Int = 30,
  analyseOnly: Boolean = false,
  runWithDebug: Boolean = false,
  overwriteResults: Boolean = true)
