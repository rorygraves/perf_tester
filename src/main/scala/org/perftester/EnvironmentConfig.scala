package org.perftester

import ammonite.ops.Path

case class EnvironmentConfig(
  checkoutDir: Path = Path("/xxx"),
  testDir: Path= Path("/xxx"),
  outputDir: Path= Path("/xxx"),
  iterations: Int = 50,
  analyseOnly: Boolean = false,
  runWithDebug: Boolean = false,
  overwriteResults: Boolean = true)
