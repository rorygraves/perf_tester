package org.perftester

import java.io.File

import ammonite.ops.Path
import scopt.OptionParser

object PerfTesterOptionParser {
  val parser: OptionParser[EnvironmentConfig] =
    new OptionParser[EnvironmentConfig]("ProfileMain") {
      head("perf_tester", "1.0")

      opt[Int]('i', "iterations")
        .action((x, c) => c.copy(iterations = x))
        .text(
          s"The number of iterations to run in each VM (default ${EnvironmentConfig().iterations})")

      opt[Int]('p', "processes")
        .action((x, c) => c.copy(processes = x))
        .text(s"The number of processes (VMs) to run(default ${EnvironmentConfig().processes})")

      opt[String]('c', "config")
        .valueName("<configName>")
        .action {
          case (x, c) =>
            assert(Configurations.configurations.contains(x),
                   s"Configuration must exist - one of ${Configurations.namesList}")
            c.copy(config = x)
        }
        .text(s"The test configuration to run - one of ${Configurations.namesList} (required)")

      opt[String]("configString")
        .valueName("<configString>")
        .action {
          case (x, c) =>
            c.copy(
              configString = x,
              config = "custom"
            )
        }
        .text(
          s"The test configuration string in format: 'id1;gitHash1;extraOption1,extraOption2,...|id2;gitHash2;extraOption1,extraOption2,...' ")

      opt[File]('s', "scalaCDir")
        .required()
        .valueName("<dir>")
        .action {
          case (x, c) =>
            assert(x.exists(), "ScalaC checkout directory must exist")
            assert(x.isDirectory, "ScalaC checkout directory must be a directory")
            c.copy(checkoutDir = Path(x.getAbsolutePath))
        }
        .text("The ScalaC checkout/build  dir (required)")

      opt[String]("user").required().valueName("<username>").action {
        case (username, config) =>
          config.copy(username = username)
      }

      opt[File]('a', "corpus")
        .required()
        .valueName("<dir>")
        .validate {
          case d if !d.exists      => Left("corpus directory must exist")
          case d if !d.isDirectory => Left("corpus directory must be a directory")
          case _                   => Right(())
        }
        .action {
          case (x, c) =>
            c.copy(testDir = Path(x.getAbsolutePath))
        }
        .text("The test project directory (required)")

      opt[File]('r', "resultsDir")
        .required()
        .valueName("<dir>")
        .validate {
          case d if !d.exists => Left("results directory must exist")
          case d if !d.isDirectory =>
            Left("results directory must be a directory")
          case _ => Right(())
        }
        .action {
          case (x, c) =>
            c.copy(outputDir = Path(x.getAbsolutePath))
        }
        .text("Where to write results files (required)")

      opt[Boolean]("overwriteResults")
        .action((b, c) => c.copy(overwriteResults = b))
        .text("Whether to overwrite previous results")

      opt[Unit]("runWithDebug")
        .action((_, c) => c.copy(runWithDebug = true))
        .text("Run with debug args (default false)")

      opt[Unit]("analyseOnly")
        .action((_, c) => c.copy(analyseOnly = true))
        .text("only run analysis (default false)")

      help("help").text("prints this usage text")

    }
}
