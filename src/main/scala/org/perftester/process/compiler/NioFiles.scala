package scala.org.perftester.process.compiler

import java.nio.file.{Path, Paths}

import scala.reflect.io.PlainNioFile

object NioFiles {
  def file(s: String) = new PlainNioFile(Paths.get(s))
}
