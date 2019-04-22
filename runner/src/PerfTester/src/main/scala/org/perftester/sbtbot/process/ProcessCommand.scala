package org.perftester.sbtbot.process

import ammonite.ops.Path

final class ProcessCommand private (
    val cmd: Seq[String],
    val envArgs: Map[String, String],
    val workingDir: Option[Path],
) {

  def withWorkingDir(workingDir: Path): ProcessCommand =
    new ProcessCommand(cmd, envArgs, Some(workingDir))

  def withEnv(key: String, value: String): ProcessCommand =
    new ProcessCommand(cmd, envArgs + (key -> value), workingDir)

  override def toString: String =
    s"ProcessCommand(cmd=$cmd,envArgs=$envArgs,workingDir=$workingDir)"
}

object ProcessCommand {
  def apply(cmd: Seq[String],
            envArgs: Map[String, String] = Map.empty,
            workingDir: Option[Path] = None): ProcessCommand = new ProcessCommand(
    cmd = cmd.filterNot(_.trim.isEmpty), // osx java breaks with empty args
    envArgs = envArgs,
    workingDir = workingDir
  )
}
