package org.perftester.process

import java.io.ObjectOutputStream

sealed trait Inputs {
  def writeTo(outputStream: ObjectOutputStream) = outputStream.synchronized {
    outputStream.writeObject(this)
  }
}

case class Run(className: String, args: Seq[String]) extends Inputs

case object Gc extends Inputs

case object Exit extends Inputs
