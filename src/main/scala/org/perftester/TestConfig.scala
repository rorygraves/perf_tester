package org.perftester

import ammonite.ops.Path

sealed trait BuildType {
  def forceOverwriteResults = false
}

case class BuildFromGit(sha: String, customPath: Option[Path] = None) extends BuildType

case class BuildFromDir(pathStr: String, override val forceOverwriteResults: Boolean = false, rebuild: Boolean = false) extends BuildType {
  def path: Path = Path(pathStr)
}

case class TestConfig(id: String, buildDefn: BuildType, extraArgs: List[String] = Nil, extraJVMArgs: List[String] = Nil)
