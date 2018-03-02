package org.perftester.process

import java.io.ObjectOutputStream

sealed trait Inputs {
  def writeTo(outputStream: ObjectOutputStream) = outputStream.synchronized {
    outputStream.writeObject(this)
  }
}

case class Run(className: String, args: Seq[String]) extends Inputs

case class ScalacGlobalConfig(id: String,
                              outputDirectory: Option[String],
                              classPath: Option[Seq[String]],
                              otherParams: Option[List[String]],
                              files: Option[List[String]])
    extends Inputs {
  override def toString =
    s"ScalacGlobalConfig[$id] ${option("outputDirectory", outputDirectory)}${option(
      "classPath",
      classPath)}${option("otherParams", otherParams)}${option("files", files)}"

  def option(name: String, value: Option[Any]) = value match {
    case None    => ""
    case Some(x) => s"$name=$x"
  }
}

case class ScalacRun(id: String) extends Inputs

case object Gc extends Inputs

case object Exit extends Inputs
