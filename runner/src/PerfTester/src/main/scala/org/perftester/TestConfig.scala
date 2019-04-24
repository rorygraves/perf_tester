package org.perftester

import ammonite.ops.Path

sealed trait BuildType {
  def forceOverwriteResults = false
}

case class BuildFromGit(baseSha: String,
                        cherryPicks: List[String] = Nil,
                        customPath: Option[Path] = None)
    extends BuildType {
  def fullShaName =
    if (cherryPicks isEmpty) baseSha else baseSha + cherryPicks.mkString("_+_", "_+_", "")
}

case class BuildFromDir(pathStr: String,
                        override val forceOverwriteResults: Boolean = false,
                        rebuild: Boolean = false)
    extends BuildType {
  def path: Path = Path(pathStr)
}

case class TestConfig(id: String,
                      buildDefn: BuildType,
                      extraArgs: List[String] = Nil,
                      extraJVMArgs: List[String] = Nil,
                      useScala2_13: Boolean = false,
                      useSbt: Boolean = true)
