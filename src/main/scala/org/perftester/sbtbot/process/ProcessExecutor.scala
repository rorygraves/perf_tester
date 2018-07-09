package org.perftester.sbtbot.process

import java.io.PrintWriter

import akka.actor.{Actor, ActorLogging, Cancellable, Props}
import ProcessExecutor.{CheckDeadlock, CheckStatus, Send}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

object ProcessExecutor {
  def props(command: ProcessCommand): Props = {
    Props(new ProcessExecutor(command))
  }

  case object CheckStatus
  case object CheckDeadlock

  case class Send(s: String)

}

/**
  * Execute a command forwarding all relevant events to the watcher (io/termination etc)
  *
  * @param command The command to execute
  */
class ProcessExecutor private (command: ProcessCommand) extends Actor with ActorLogging {

  implicit val ec: ExecutionContext                    = context.system.dispatcher
  private var process: Option[Process]                 = None
  private var outReader: Option[InputStreamLineReader] = None
  private var errReader: Option[InputStreamLineReader] = None
  private var inWriter: Option[PrintWriter]            = None

  var timers: Seq[Cancellable] = Nil

  override def preStart(): Unit = {
    super.preStart()
    log.info("Starting")
    startProcess()
  }

  var processTerminated = false
  var outTerminated     = false
  var errTerminated     = false
  var seenLog           = false

  private def processIOStream(isr: Option[InputStreamLineReader], ioSource: IOSource): Unit = {
    isr.foreach { r =>
      r.read().foreach { line =>
        context.parent ! ProcessIO(ioSource, line)
        seenLog = true
      }
    }
  }

  private def closeIOStream(isr: Option[InputStreamLineReader], ioSource: IOSource): Unit = {
    isr.foreach { r =>
      r.close().foreach { line =>
        context.parent ! ProcessIO(ioSource, line)
        seenLog = true
      }

    }
  }

  def check(): Unit = {
    processIOStream(outReader, StdOutIOSource)
    processIOStream(errReader, StdErrIOSource)
    process match {
      case Some(p) =>
        // calling exitValue and handling failure works better than isComplete apparently.
        Try(p.exitValue()) match {
          case Success(exitCode) =>
            closeIOStream(outReader, StdOutIOSource)
            closeIOStream(errReader, StdErrIOSource)
            context.parent ! ProcessExited(exitCode)
            log.info(s"Process exited with exit code $exitCode")
            context.stop(self)
          case Failure(_) =>
          // do nothing
        }

      case None =>
        log.error("Illegal state, should not be in check without process")
    }
  }

  private val sbtJVMNames = Set("sbt-launch.jar", "Boot")

  def takeASnapshot(): Unit = {
    val out = {
      import sys.process._
      "jps".!!
    }
    val pids =
      out.lines.toList.map(_.split(" ")).filter(row => sbtJVMNames.contains(row(1))).map(_.apply(0))
    if (pids.isEmpty) {
      println(s"### No sbt jvm running in:\n $out")
    }
    pids.foreach { pid =>
      println(s"#### Stacktrace of sbt project of pid $pid ####")
      println {
        import sys.process._
        s"jstack $pid".!!
      }
    }
  }

  override def receive: Receive = {
    case CheckStatus =>
      check()
    case CheckDeadlock =>
      try if (!seenLog) takeASnapshot()
      finally seenLog = false
    case Send(text) =>
      inWriter.foreach { w =>
        w.println(text)
        w.flush()
      }
    case m =>
      log.error(s"Unknown message received $m from $sender")
      context.parent ! ProcessError(s"Unknown message $m")
      context.stop(self)
  }

  override def postStop(): Unit = {
    log.info("postStop")
    outReader.foreach { s =>
      Try(s.close())
    }
    errReader.foreach { s =>
      Try(s.close())
    }
    process.foreach {
      _.destroyForcibly()
    }
    timers.foreach(_.cancel())
  }

  def startProcess(): Unit = {
    log.info("Starting process " + command)

    val builder = new java.lang.ProcessBuilder()
    builder
      .environment()
      .putAll(collection.JavaConverters.mapAsJavaMap(command.envArgs))

    // set working dir if defined
    command.workingDir.foreach { wd =>
      builder.directory(new java.io.File(wd.toString))
    }

    val process = builder.command(command.cmd: _*).start()
    this.process = Some(process)
    // create all the things
    outReader = Some(new InputStreamLineReader(process.getInputStream, "OUT"))
    errReader = Some(new InputStreamLineReader(process.getErrorStream, "ERR"))
    inWriter = Some(new PrintWriter(process.getOutputStream))
    log.info("Process started successfully")
    import scala.concurrent.duration._
    timers = Seq(
      context.system.scheduler.schedule(250.millis, 200.millis, context.self, CheckStatus),
      context.system.scheduler.schedule(250.millis, 5.minutes, context.self, CheckDeadlock)
    )
  }
}
