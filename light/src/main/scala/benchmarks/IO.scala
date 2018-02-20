package benchmarks

import java.nio.file.{Files, Path}

/* This is fast hacky IO but it prove to be sufficient */
object IO {
  def cleanDir(dir: Path): Unit = if(Files.exists(dir))
    Files.walk(dir, 50).toArray.sortBy(- _.toString.length).foreach(o => Files.delete(o.asInstanceOf[Path]))


  def jarsIn(path: Path): Seq[Path] =
    Files.walk(path).toArray().map(_.asInstanceOf[Path].toAbsolutePath)
      .toList.filter(_.getFileName.toString.endsWith(".jar"))

  def listSourcesIn(path: Path): List[Path] = {
    def isSource(p: Path) = {
      val name = p.getFileName.toString
      name.endsWith(".scala") || name.endsWith(".java")
    }
    val maxDepth = 557
    Files.walk(path, maxDepth).toArray.map(_.asInstanceOf[Path].toAbsolutePath).filter(isSource).toList
  }
}
