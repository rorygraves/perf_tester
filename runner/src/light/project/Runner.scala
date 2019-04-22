import java.io.File
import java.nio.file.Paths

import Benchmarks.benchOutput
import com.typesafe.config.{Config, ConfigFactory, ConfigObject}
import sbt._
import sbt.Keys._

import scala.util.Try
import scala.sys.process._
import collection.JavaConverters._

case class Configuration(
                        reference: String,
                        baseScalaVersion: String,
                        buildLocally: Boolean,
                        jvmOptions: String,
                        scalaOptions: String
                        ){
  val scalaVersion = if(buildLocally) s"$baseScalaVersion-$reference-SNAPSHOT" else reference
}

object Runner {
  val scalaRepoRef = SettingKey[String]("scalaRepoRef")
  val scalaRepoLocation = SettingKey[File]("scalaRepoLocation")
  val installScalaCommits = TaskKey[Unit]("installScalaCommits")
  val generateBenchmarkRunner = TaskKey[Unit]("generateBenchmarkRunner")
  val configurationPath = SettingKey[String]("configurationPath")
  val benchmarks = SettingKey[Map[String, Configuration]]("benchmarks")
  val iterations = TaskKey[Int]("iterations")

  def settings = Seq(
    configurationPath := "benchmark.conf",
    implementInstallScalaCommits,
    scalaRepoLocation := file(".") / "scalaRepo",
    scalaRepoRef := "git@github.com:rorygraves/scalac_perf.git",
    implementInterations,
    implementConfigs,
    implementGenerateBenchmarkRunner,
    scalaVersion := { benchmarks.value.head match {
      case (_, c) if c.scalaVersion.startsWith("2.12") =>
        "2.12.4"
      case _ =>
        "2.11.11"
    }}
  )

  def implementInstallScalaCommits = installScalaCommits := {
    // TODO skip is commits are empty
    val location = scalaRepoLocation.value
    def run(op: String*): Boolean = {
      assert(Process(op, location).! == 0, s"Failed command: $op")
      true
    }

    if (location.exists() && Try(run("git", "status")).get){
      run("git", "fetch")
    } else {
      val reference = scalaRepoRef.value
      s"git clone $reference $location".!!
    }

    benchmarks.value.foreach {
      case (_, config) =>
        if (config.buildLocally){
          run(s"git", "checkout", config.reference)
          run("sbt", "clean",
            s"""set version.in(ThisBuild) := "${config.scalaVersion}"""", "library/publishLocal", "compiler/publishLocal", "reflect/publishLocal")
        }
    }
  }

  private def readConfig(path: String): Config = ConfigFactory.parseFile(Paths.get(path).toFile)

  def implementInterations = iterations := readConfig(configurationPath.value).getInt("iterations")

  def implementConfigs = benchmarks := readConfig(configurationPath.value).getObject("benchmarks").asScala.map {
    case (name, obj: ConfigObject) =>
      def read(name: String, default: String) = Try(obj.toConfig.getString(name)).getOrElse(default)

      name -> Configuration(
        reference = read("reference", name),
        baseScalaVersion = read("baseScalaVersion", "2.12.4"),
        buildLocally = read("buildLocally", "false").toBoolean,
        jvmOptions = read("jvmOptions", ""),
        scalaOptions = read("scalaOptions", "")
      )
  }(collection.breakOut)

  def implementGenerateBenchmarkRunner = generateBenchmarkRunner := {
    val benchmarkDir = file(".") / "benchOut"
    val jars = dependencyClasspath.in(Compile).value.map(_.data) :+ Keys.`package`.in(Compile).value
    val jarsMapping = jars.map(f => f ->  benchmarkDir / "jars" / f.getName)
    jarsMapping.foreach(f => IO.copyFile(f._1,f._2))
    IO.copyFile(Paths.get(configurationPath.value).toFile, benchmarkDir / "benchmark.conf")

    val appClasspath = jarsMapping.map(_._2).map(f => benchmarkDir.toPath.relativize(f.toPath))

    val scriptLines = Seq(
      "#!/bin/bash",
      "cd `dirname $0`",
      s"java -cp ${appClasspath.mkString(File.pathSeparator)} org.preftester.Main $$@"
    )


    val bashScriptFile = benchmarkDir / "run.sh"

    IO.write(bashScriptFile, scriptLines.mkString("\n"))
    bashScriptFile.setExecutable(true)
  }

}
