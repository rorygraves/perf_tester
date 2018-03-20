package org.perftester.process

import java.nio.file.{Files, Path, SimpleFileVisitor, FileVisitResult}
import java.nio.file.attribute.BasicFileAttributes
import java.io.IOException

/* This is fast hacky IO but it prove to be sufficient */
object IO {

  def deleteDir(file: Path): Unit = {

    object deleter extends SimpleFileVisitor[Path] {
      override def visitFile(path: Path, attr: BasicFileAttributes): FileVisitResult = {
        Files.delete(path)
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(path: Path, e: IOException): FileVisitResult = {
        if (e eq null) {
          Files.delete(path)
          FileVisitResult.CONTINUE
        } else throw e // directory iteration failed
      }
    }

    Files.walkFileTree(file, deleter)
  }

  def jarsIn(path: Path): Seq[Path] =
    Files
      .walk(path)
      .toArray()
      .map(_.asInstanceOf[Path].toAbsolutePath)
      .toList
      .filter(_.getFileName.toString.endsWith(".jar"))

  def listSourcesIn(path: Path): List[Path] = {
    def isSource(p: Path) = {
      val name = p.getFileName.toString
      name.endsWith(".scala") || name.endsWith(".java")
    }
    val maxDepth = 557
    Files
      .walk(path, maxDepth)
      .toArray
      .map(_.asInstanceOf[Path].toAbsolutePath)
      .filter(isSource)
      .toList
  }
}
