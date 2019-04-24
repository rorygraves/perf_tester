package scala.hacks

import java.nio.file.Path

import scala.reflect.io.PlainNioFile

object NioFile {
  def apply(path: Path) = new PlainNioFile(path)
}
