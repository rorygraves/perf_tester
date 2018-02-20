package benchmarks

import java.io.File
import java.nio.file.{Files, Path}

import benchmarks.Main.rootPath

import scala.reflect.internal.util.Position
import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.Reporter

case class CompilerSetup(rootPath: Path) {
  val outputDir: Path = rootPath.resolve("output")
  val currentOutput: Path = outputDir.resolve("output")

  IO.cleanDir(outputDir)
  Files.createDirectories(currentOutput)


  val cpJars = IO.jarsIn(rootPath.resolve("cpJars"))

  val reporter: Reporter = new Reporter { // We are ignoring all
    override protected def info0(pos: Position, msg: String, severity: this.Severity, force: Boolean): Unit = {
    //   println(s"[$severity] $pos: $msg") // Uncomment for to get compilation messages
    }
  }

  val settings: Settings = new Settings( _ => ???)
  configure(settings)

  val global: Global = new Global(settings, reporter)

  def configure(settings: Settings): Unit = {
    settings.outputDirs.setSingleOutput(currentOutput.toString)
    settings.classpath.append(cpJars.mkString(File.pathSeparator))
  }
}
