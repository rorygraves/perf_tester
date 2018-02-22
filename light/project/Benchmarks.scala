import java.io.File

import sbt._
import sbt.Keys._

object Benchmarks {
  val libToTest =  SettingKey[ModuleID]("libToTest")
  val benchOutput = SettingKey[File]("benchOutput")
  val produceBench = TaskKey[File]("produceBench")
  val runBench = TaskKey[Unit]("runBench")
  val enableScalacProfiler = SettingKey[Boolean]("enableScalacProfiler")
  val scalacProfilerOutput = SettingKey[File]("scalacProfilerOutput")


  def settings = Seq(
    // TODO separation between deps and benchmark
    libraryDependencies := Seq(libToTest.value.withSources(), "org.scala-lang" % "scala-compiler" % scalaVersion.value),
    benchOutput := file(".") / "benchOut" / scalaVersion.value,
    scalacProfilerOutput := benchOutput.value / "scalacProfilerOutput",
    enableScalacProfiler := true,
    createBenchImpl,
    runBenchImpl
  )

  def createBenchImpl = produceBench := {
    val dest = benchOutput.value.getAbsoluteFile
    Option(dest.listFiles()).foreach(_.foreach(IO.delete))

    // TODO add proper support for scala versions mangling
    val libToTest = Benchmarks.libToTest.value.withName(Benchmarks.libToTest.value.name + "_2.12")
    def isLibToTest(m: ModuleReport) =
      m.module.organization == libToTest.organization && m.module.name == libToTest.name

    def isScalaDep(m: ModuleReport) = m.module.organization == "org.scala-lang"
    def compileConfig(u: UpdateReport) = u.configurations.find(_.configuration.name == "compile").get

    val sourceJar =
      compileConfig(updateClassifiers.value).modules.find(isLibToTest).get.artifacts.collectFirst {
        case (artifact, file) if artifact.classifier == Some("sources") =>
          file
      }.get

    IO.unzip(sourceJar, dest / "sources")

    def jarsIn(moduleReport: ModuleReport) = moduleReport.artifacts.map(_._2)

    val (scalaDeps, cpDeps) = compileConfig(update.value).modules.filterNot(isLibToTest).partition(isScalaDep)

    val destBechJar = dest / "bench.jar"
    IO.copyFile(Keys.`package`.in(Compile).value, destBechJar)
    val scalaJarsMapping = scalaDeps.flatMap(jarsIn).map(d => (d, dest / "scalaJars" / d.getName))
    scalaJarsMapping.foreach{ case (origin, dest) => IO.copyFile(origin, dest) }

    // TODO add support for libs that declare scala compiler as dep
    val scalaLib = scalaDeps.filter(_.module.name == "scala-library")
    (cpDeps ++ scalaLib).flatMap(jarsIn).foreach(d => IO.copyFile(d, dest / "cpJars" / d.getName))

    val appClasspath = (scalaJarsMapping.map(_._2) ++ Seq(destBechJar)).map(f => dest.toPath().relativize(f.toPath))

    // TODO add more scripts (run bench M times etc.)
    val scriptLines = Seq(
      "#!/bin/bash",
      "cd `dirname $0`",
      s"java -cp ${appClasspath.mkString(File.pathSeparator)} benchmarks.Main"
    )


    val bashScriptFile = dest / "run.sh"

    IO.write(bashScriptFile, scriptLines.mkString("\n"))
    bashScriptFile.setExecutable(true)

    streams.value.log.success(s"Benchmark was created in ${dest.toPath}")

    // TODO add code generation for params java options and other things
    bashScriptFile.getAbsoluteFile
  }

  def runBenchImpl = runBench := {
    val script = produceBench.value.getAbsolutePath.toString
    streams.value.log.success(s"Running benchmark from $script")
    import scala.sys.process._
    script.!
  }

}
