package org.perftester.sbtbot

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import ammonite.ops.{Path, write}
import org.perftester.sbtbot.SBTBot._
import org.perftester.sbtbot.process._

object SBTBot {

  sealed trait SBTBotMessage
  final case class SBTError(msg: String) extends SBTBotMessage
  final case class SBTExited(exitCode: Int) extends SBTBotMessage
  case object SBTBotReady extends SBTBotMessage

  final case class ExecuteTask(id: String, task: String)

  final case class TaskResult(id: String, io: Vector[String])

  /**
    * An execution request - command line is the same as typed in at the command prompt
    *
    * @param cmd The sbt command to execute (e.g. 'clean')
    * @param id Client generated unique id, related response events will carry this id.
    */
  final case class SBTCommand(cmd: String, id: String)


  def props(workspaceRootDir: Path, sbtArgs: List[String], jvmArgs: List[String]): Props = {
    Props(new SBTBot(workspaceRootDir, sbtArgs, jvmArgs))
  }
}

class SBTBot private (workspaceRootDir: Path,
                      sbtArgs: List[String],
                      jvmArgs: List[String]
                     ) extends Actor with ActorLogging {

  import java.util.UUID
  private val promptStr = UUID.randomUUID().toString

  def sbtCommandLine(extraJVMArgs: List[String]): List[String] = {
    val sbt = new File("sbtlib/sbt-launch.jar").getAbsoluteFile
    require(sbt.exists(),"sbt-launch.jar must exist in sbtlib directory")
    val raw = List("java","-Dfile.encoding=UTF8", "-Xmx12G", "-XX:MaxPermSize=256m", "-XX:ReservedCodeCacheSize=128m", "-Dsbt.log.format=true", "-mx12G") ::: extraJVMArgs ::: List("-cp", sbt.toString, "xsbt.boot.Boot")
    raw
  }
  private val execCommandArgs: List[String] = sbtCommandLine(jvmArgs) :::
    List(
      "-Dsbt.log.noformat=true",
      s"""set shellPrompt := ( _ =>  "$promptStr")""") :::
    sbtArgs ::: List("shell") map escape

  def escape(s:String) = s.replace("\\", "\\\\").replace("\"", "\"\"")

  private var sbtProcess: ActorRef = _
  private val execCommand = ProcessCommand(execCommandArgs).withWorkingDir(workspaceRootDir)

  override def preStart: Unit = {
//    write.over(workspaceRootDir / "project" / "build.properties", "sbt.version=1.0.2")
//    write.over(workspaceRootDir / "project" / "plugins.sbt", "")
    log.info(s"Executing command: $execCommand")
    sbtProcess = context.actorOf(ProcessExecutor.props(execCommand), "exec")
  }

  override def receive: Receive = initReceive

  def initReceive: Receive = {

    case ProcessError(error) =>
      log.warning(s"Got process error $error")
      context.parent ! SBTError(error)
    case ProcessExited(exitCode) =>
      log.warning(s"Got exit code $exitCode")
      context.parent ! SBTExited(exitCode)
    case ProcessIO(source, content) =>
      log.info(s"SBTBOT:${source.shortName}: $content")
      if (content == promptStr) {
        context.parent ! SBTBotReady
        context.become(idleReceive, discardOld = true)
      }
    case x => log.info("Don't know what to do with {}", x)
  }

  def sendToSBT(c: SBTCommand): Unit = {
    sbtProcess ! ProcessExecutor.Send(c.cmd)
  }

  def idleReceive: Receive = {
    case ExecuteTask(id, task) =>
      startTask(id, task, context.sender)
    case ProcessError(error) =>
      log.warning(s"IDLE Got process error $error")
      context.parent ! SBTError(error)
    case ProcessExited(exitCode) =>
      log.warning(s"IDLE Got exit code $exitCode")
      context.parent ! SBTExited(exitCode)
    case ProcessIO(source, content) =>
      log.info(s"IDLE Got process IO $source $content")
    case x =>
      log.info("IDLE Don't know what to do with {}", x)
  }

  override def postStop(): Unit = {
    log.info("POST STOP -----------------------------------")
    sendToSBT(SBTCommand("exit", "SYSTEM"))
    Thread.sleep(500)
    sbtProcess ! PoisonPill
    log.info("POST STOP -----------------------------------")
  }

  private var requestor: Option[ActorRef] = None
  private var currentTaskId = "UNKNWON"
  private var io = Vector[String]()
  private var seenComplete = false
  private var seenIOTotalTime = false


  def startTask(taskId: String, task: String, requestor: ActorRef): Unit = {
    sendToSBT(SBTCommand(task, taskId))
    this.requestor = Some(requestor)
    this.currentTaskId = taskId
    seenComplete = false
    io = Vector.empty
    seenIOTotalTime = false
    context.become(activeReceive, discardOld = true)
  }

  def checkActiveComplete(): Unit = {
    if (seenIOTotalTime && seenComplete) {
      log.info(s"Tast $currentTaskId complete")
      requestor.foreach(_ ! TaskResult(currentTaskId, io))
      currentTaskId = "UNKNOWN"
      requestor = None
      io = Vector.empty
      context.become(idleReceive, discardOld = true)
    }
  }
  def activeReceive: Receive = {
    case ProcessError(error) =>
      log.warning(s"ACT Got process error $error")
      context.parent ! SBTError(error)
    case ProcessExited(exitCode) =>
      log.warning(s"ACT Got exit code $exitCode")
      context.parent ! SBTExited(exitCode)
    case ProcessIO(source, content) =>
      log.info(s"ACT Got process IO $source $content")
      io :+= content
      if (io.last.contains("] Total time: ")) {
        seenIOTotalTime = true
      }
      if(io.last == promptStr) {
        seenComplete = true
        checkActiveComplete()
      }
    case x =>
      log.info("ACT Don't know what to do with {}", x)
  }

}
