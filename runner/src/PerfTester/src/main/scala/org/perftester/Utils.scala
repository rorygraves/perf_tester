package org.perftester

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import java.time.Instant
import java.util

import ammonite.ops.{Path => aPath}

object Utils {
  def lastChangedDate(path: aPath): (Instant, String) =
    lastChangedDate(path.toNIO)

  def lastChangedDate(path: Path): (Instant, String) = {
    var latest = Files.getLastModifiedTime(path)
    var at     = path.toString

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
  def deleteDir(scalaPackDir: Path) = {
    if (Files.exists(scalaPackDir)) {
      println(s"delete pack dir $scalaPackDir")
      Files.walkFileTree(scalaPackDir, fileDeleter)
    } else {
      println(s"pack dir $scalaPackDir doesnt exist")
    }
  }
  private object fileDeleter extends SimpleFileVisitor[Path] {

    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
      println(s"delete file $file")
      Files.delete(file)
      FileVisitResult.CONTINUE
    }

    override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
      println(s"delete dir $dir")
      Files.delete(dir)
      FileVisitResult.CONTINUE
    }
  }
  def copy(source: Path, target: Path): Unit = {
    class Copier(source: Path, target: Path) extends SimpleFileVisitor[Path] {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        val targetDir = target.resolve(source.relativize(dir))
        println(s"copy dir $dir -> $targetDir")
        Files.copy(dir, targetDir)
        FileVisitResult.CONTINUE
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        val targetFile = target.resolve(source.relativize(file))
        println(s"copy file $file -> $targetFile")
        Files.copy(file, targetFile)
        FileVisitResult.CONTINUE
      }
    }

    Files.walkFileTree(source, new Copier(source, target))
  }
  def touch(path: Path): Unit = {
    if (Files.exists(path)) Files.setLastModifiedTime(path, FileTime.from(Instant.now))
    else Files.createFile(path)
  }

}
