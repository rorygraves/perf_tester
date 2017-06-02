package org.perftester

import ammonite.ops.Path

sealed trait BuildType {
  def forceOverwriteResults = false
}

case class BuildFromGit(sha: String, customPath: Option[Path] = None) extends BuildType

object BuildFromDir {
  def apply(path:String, forceOverwrite:Boolean = false): BuildFromDir = BuildFromDir(Path(path), forceOverwrite)
}

case class BuildFromDir(path: Path, override val forceOverwriteResults:Boolean) extends BuildType

case class TestConfig(id: String, buildDefn: BuildType, extraArgs: List[String] = Nil, extraJVMArgs: List[String] = Nil)
