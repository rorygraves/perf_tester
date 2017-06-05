package org.perftester

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import java.time.Instant
import java.util

import ammonite.ops.{Path => aPath}

/**
  * Created by dev on 05/06/2017.
  */
object Utils {
  def lastChangedDate(path:aPath): (Instant, String) = lastChangedDate(path.toNIO)
  def lastChangedDate(path:Path): (Instant, String) = {
    var latest = Files.getLastModifiedTime(path)
    var at = path.toString

    object walker extends SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        val thisTime = attrs.lastModifiedTime()
        if (thisTime.compareTo(latest) > 0) {
          at = file.toString
          latest = thisTime
        }
        FileVisitResult.CONTINUE
      }

      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if (dir.getFileName.toString == "intellij")
          FileVisitResult.SKIP_SUBTREE
        else FileVisitResult.CONTINUE
      }
    }
    Files.walkFileTree(path, util.EnumSet.noneOf(classOf[FileVisitOption]), Int.MaxValue, walker)
    (latest.toInstant, at)
  }
}
