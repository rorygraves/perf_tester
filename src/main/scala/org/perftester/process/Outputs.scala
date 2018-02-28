package org.perftester.process

import java.io.{ObjectOutputStream, OutputStream}

import scala.util.Try

sealed trait Outputs {
  def writeTo(outputStream: ObjectOutputStream) = outputStream.synchronized {
    outputStream.writeObject(this)
  }
}

case class Console(err: Boolean, text: Array[Byte]) extends Outputs

case class Complete(input: Inputs, result: Try[Unit]) extends Outputs
