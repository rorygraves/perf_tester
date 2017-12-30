package org.perftester.sbtbot.process

sealed trait IOSource {
  def shortName: String
}

case object StdErrIOSource extends IOSource {
  val shortName = "err"
}

case object StdOutIOSource extends IOSource {
  val shortName = "out"
}

sealed trait ProcessMessage

case class ProcessError(error: String) extends ProcessMessage

case class ProcessExited(exitCode: Int) extends ProcessMessage

case class ProcessIO(source: IOSource, content: String) extends ProcessMessage
