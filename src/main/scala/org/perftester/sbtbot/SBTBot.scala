package org.perftester.sbtbot

import java.io.File
import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import ammonite.ops.{Path, read}
import org.perftester.ProfileMain
import org.perftester.sbtbot.SBTBot._
import org.perftester.sbtbot.process._
import play.api.libs.json.Json

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
    * @param id  Client generated unique id, related response events will carry this id.
    */
  final case class SBTCommand(cmd: String, id: String)


  def props(workspaceRootDir: Path, sbtArgs: List[String], jvmArgs: List[String]): Props = {
    Props(new SBTBot(workspaceRootDir, sbtArgs, jvmArgs))
  }
}

class SBTBot private(workspaceRootDir: Path,
                     sbtArgs: List[String],
                     jvmArgs: List[String]
                    ) extends Actor with ActorLogging {

  import java.util.UUID

  implicit val as: ActorSystem = context.system
  private val promptStr = UUID.randomUUID().toString

  def sbtCommandLine(extraJVMArgs: List[String]): List[String] = {
    val sbt = new File("sbtlib/sbt-launch.jar").getAbsoluteFile
    require(sbt.exists(), "sbt-launch.jar must exist in sbtlib directory")
    val raw = List("java", "-Dfile.encoding=UTF8", "-Xmx12G", "-XX:MaxPermSize=256m", "-XX:ReservedCodeCacheSize=128m", "-Dsbt.log.format=false",
      "-mx12G") ::: extraJVMArgs ::: List("-cp", sbt.toString, "xsbt.boot.Boot")
    raw
  }

  private val execCommandArgs: List[String] = sbtCommandLine(jvmArgs) :::
    List(
      "-Dsbt.log.noformat=true" /*,
      s"""set shellPrompt := ( _ =>  \"$promptStr\n\")"""*/) :::
    sbtArgs ::: List(s"""set shellPrompt := ( _ =>  "$promptStr" + System.getProperty("line.separator"))""", "shell") map escape

  def escape(s: String): String = if (ProfileMain.isWindows) s.replace("\\", "\\\\").replace("\"", "\"\"") else s

  private var sbtProcess: ActorRef = _
  private val execCommand = ProcessCommand(execCommandArgs).withWorkingDir(workspaceRootDir)

  override def preStart: Unit = {
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
      if (content.contains(promptStr)) {
        connectToServer()
      }
    case x => log.info("Don't know what to do with {}", x)
  }

  def connectToServer(): Unit = {
    val activeJsonContests = read ! workspaceRootDir / "project" / "target" / "active.json"
    val activeJson = Json.parse(activeJsonContests)
    val tokenFilePath = (activeJson \ "tokenfilePath").as[String]

    val tokenFile = read ! Path(tokenFilePath)
    val tokenJson = Json.parse(tokenFile)
    val uri = (tokenJson \ "uri").as[String]
    val token = (tokenJson \ "token").as[String]

    val shortUrl = uri.replace("tcp://", "")
    val split = shortUrl.split(":")
    val host = split(0)
    val port = split(1).toInt
    val socketAddr = new InetSocketAddress(host, port)
    IO(Tcp) ! Connect(socketAddr)
    context.become(serverConnectReceive(token))
  }

  var connection: ActorRef = _

  var currentBytes = ByteString()

  var seenJsonInit = false
  var seenInitPrompt = false

  def serverConnectReceive(token: String): Receive = {
    case CommandFailed(_: Connect) =>
      context.parent ! SBTError("Failed to connect")
      context stop self
    case c@Connected(remote, local) ⇒
      println("CONNECTED")
      connection = sender()
      connection ! Register(self)
      connection ! Write(ByteString(
        s"""{ "jsonrpc": "2.0", "id": 1, "method": "initialize", "params": { "initializationOptions": { "token": "$token" } } }
           |""".stripMargin))
    case Received(bs) =>
      currentBytes = currentBytes ++ bs
      val (messages, newBytes) = extractMessages(currentBytes)
      currentBytes = newBytes
      messages.foreach { m => println(" MESSAGE = " + m) }
      if (messages.exists(_.contains("\"Done\""))) {
        log.info("Seen done")
        seenJsonInit = true
        checkInitComplete()
      }
    case ProcessIO(source, content) =>
      log.info(s"ACT Init process IO $source $content")
      io :+= content
      println("io.last = " + io.last)
      if (io.last.contains("Total time:")) {
        println("SEEN INIT COMPLETE")
        seenInitPrompt = true
        checkInitComplete()
      }
    case x =>
      println("AAAGGGGHHHH " + x)
  }

  def checkInitComplete(): Unit = {
    if ( /*seenInitPrompt && */ seenJsonInit) {
      println("READY!")
      context.parent ! SBTBotReady
      context.become(idleReceive, discardOld = true)
    }

  }

  def takeLine(bs: ByteString): Option[(String, ByteString)] = {
    val idx = bs.indexOf('\n')
    if (idx != -1) {
      val (start, rest) = bs.splitAt(idx)
      var startStrVal = start.utf8String.trim
      Some(startStrVal, rest.drop(1))
    } else
      None
  }

  /**
    * Parse a 'Contest-Length: XXXX' value to retrieve the XXXX value
    *
    * @param str the incoming content length string
    * @return The XXXX part as an integer
    */
  def parseLength(str: String): Int = {
    val lastSpace = str.lastIndexOf(' ')
    str.drop(lastSpace + 1).toInt
  }

  def takeMessage(input: ByteString, bytes: Int): Option[(String, ByteString)] = {
    if (input.length >= bytes) {
      val message = input.take(bytes).utf8String
      val rest = input.drop(bytes)
      Some(message, rest)
    } else
      None
  }

  private def extractMessages(byteString: ByteString): (List[String], ByteString) = {
    val lines = for {
      (line1, rest1) <- takeLine(byteString)
      len = parseLength(line1)
      (line2, rest2) <- takeLine(rest1)
      (line3, rest3) <- takeLine(rest2)
      (message, remainder) <- takeMessage(rest3, len)
    } yield (message, remainder)

    lines match {
      case Some((message, remainder)) =>
        //        println(s"Message = '$message'")
        //        println(s"remainder =  = '${remainder.utf8String}'")
        currentBytes = remainder
        val (r1, rbs) = extractMessages(remainder)
        (message :: r1, rbs)
      case None =>
        (Nil, byteString)
    }
  }

  def sendToSBT(c: SBTCommand): Unit = {
    log.info(s"Sending command $c")
    connection ! Write(ByteString(
      s"""{ "jsonrpc": "2.0", "id": 2, "method": "sbt/exec", "params": { "commandLine": "${c.cmd}" } }
         |""".stripMargin))
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
  private var seenDone = false
  private var seenComplete = false
  private var seenIOTotalTime = false


  def startTask(taskId: String, task: String, requestor: ActorRef): Unit = {
    sendToSBT(SBTCommand(task, taskId))
    this.requestor = Some(requestor)
    this.currentTaskId = taskId
    seenDone = false
    seenComplete = false
    io = Vector.empty
    seenIOTotalTime = false
    context.become(activeReceive, discardOld = true)
  }

  def checkActiveComplete(): Unit = {
    if (seenIOTotalTime /* && seenComplete*/ && seenDone) {
      log.info(s"Task $currentTaskId complete")
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
      if (io.last.contains("Total time: ")) {
        println("SEEN TOTAL")
        seenIOTotalTime = true
        checkActiveComplete()
      } else if (io.last.contains(promptStr)) {
        println("SEEN COMPLETE")
        seenComplete = true
        checkActiveComplete()
      }
    case Received(bs) =>
      currentBytes = currentBytes ++ bs
      val (messages, newBytes) = extractMessages(currentBytes)
      currentBytes = newBytes
      messages.foreach { m => println(" MESSAGE = " + m) }
      if (messages.exists(_.contains("\"Done\""))) {
        println("SEEN DONE!")
        seenDone = true
        checkActiveComplete()
      }

    case x =>
      log.info("ACT Don't know what to do with {}", x)
  }

}
