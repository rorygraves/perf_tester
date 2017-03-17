package org.perftester

import ammonite.ops.Path

case class EnvironmentConfig(checkoutDir: Path, testDir: Path, outputDir: Path, iterations: Int)
