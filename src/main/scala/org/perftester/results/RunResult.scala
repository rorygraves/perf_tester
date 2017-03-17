package org.perftester.results

import org.perftester.TestConfig

case class RunResult(testConfig: TestConfig, iteration: Int, data: SingleExecutionResult)
