package org.perftester

import ammonite.ops.Path

sealed trait BuildType {
  def forceOverwriteResults = false
}

case class BuildFromGit(sha: String, customPath: Option[Path] = None) extends BuildType

object BuildFromDir {
  def apply(path:String, forceOverwrite:Boolean = false, rebuild : Boolean = false): BuildFromDir = BuildFromDir(Path(path), forceOverwrite, rebuild)
}

case class BuildFromDir(path: Path, override val forceOverwriteResults:Boolean, rebuild : Boolean) extends BuildType

case class TestConfig(id: String, buildDefn: BuildType, extraArgs: List[String] = Nil, extraJVMArgs: List[String] = Nil)
