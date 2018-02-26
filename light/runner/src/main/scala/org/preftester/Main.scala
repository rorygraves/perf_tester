package org.preftester

import java.io.File
import java.nio.file.{Files, Paths}

import com.typesafe.config.{ConfigFactory, ConfigObject, ConfigParseOptions}
import org.perftester.results.renderer.TextRenderer
import org.perftester.results.{ResultReader, RunResult}

import scala.collection.JavaConverters._
import scala.sys.process.Process
import scala.util.{Random, Try}

object Main extends App {
  val baseDir = Paths.get(args.headOption.getOrElse("."))

  case class Configuration(
                            reference: String,
                            baseScalaVersion: String,
                            buildLocally: Boolean,
                            jvmOptions: String,
                            scalaOptions: String
                          ){
    val scalaVersion = if(buildLocally) s"$baseScalaVersion-$reference-SNAPSHOT" else reference
  }

  val config = ConfigFactory.parseFile(
    baseDir.resolve("benchmark.conf").toFile,
    ConfigParseOptions.defaults().setAllowMissing(false)
  )

  val benchmarks = config.getObject("benchmarks").asScala.map {
    case (name, obj: ConfigObject) =>
      def read(name: String, default: String) = Try(obj.toConfig.getString(name)).getOrElse(default)

      name -> Configuration(
        reference = read("reference", name),
        baseScalaVersion = read("baseScalaVersion", "2.12.4"),
        buildLocally = read("buildLocally", "false").toBoolean,
        jvmOptions = read("jvmOptions", ""),
        scalaOptions = read("scalaOptions", "")
      )
  }.toSeq

  val iterations = config.getInt("iterations")
  val N = config.getInt("N")
  val M = config.getInt("M")

  val results = (1 to iterations).foldLeft(Map.empty[String, Vector[RunResult]]){
    case (all, i) =>
      Random.shuffle(benchmarks).foldLeft(all){
        case (all, (name, benchmark)) =>
          val location = baseDir.resolve(benchmark.scalaVersion)
          val cmd = Seq(s"./run.sh", ".", N, M, benchmark.scalaOptions).map(_.toString)
          println(s"## Run $i for $name")
          val env = if(benchmark.jvmOptions.isEmpty) Nil else Seq("_JAVA_OPTIONS" -> benchmark.jvmOptions)
          val output = Process(cmd, location.toFile, env:_*).!!
          println(output)
          val resultsDir = location.resolve("output").resolve("profile.txt")
          if (Files.exists(resultsDir)){
            val result = ResultReader.readResults(name, resultsDir, N)
            val previous = all.getOrElse(name, Vector.empty)
            all + (name -> (previous :+ result))
          } else all

      }
  }
  results.foreach{ case (name, results) =>
    println(s"########## Result for $name ##########")
    TextRenderer.outputTextResults(iterations, results)
  }
}
