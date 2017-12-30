package org.perftester.sbtbot.process

import ammonite.ops.Path

case class ProcessCommand(
                           cmd: Seq[String],
                           envArgs: Map[String, String] = Map.empty,
                           workingDir: Option[Path] = None
                         ) {

  def withWorkingDir(workingDir: Path): ProcessCommand = this.copy(workingDir = Some(workingDir))

  def withEnv(key: String, value: String): ProcessCommand = this.copy(envArgs = envArgs + (key -> value))
}

