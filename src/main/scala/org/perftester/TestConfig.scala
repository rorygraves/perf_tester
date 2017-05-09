package org.perftester

import ammonite.ops.Path

sealed trait BuildType

case class BuildFromGit(sha: String, customPath: Option[Path] = None) extends BuildType

case class BuildFromDir(path: Path) extends BuildType

case class TestConfig(id: String, buildDefn: BuildType, extraArgs: List[String] = Nil, extraJVMArgs: List[String] = Nil)
