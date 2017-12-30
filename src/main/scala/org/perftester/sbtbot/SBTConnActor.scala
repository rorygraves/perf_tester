package org.perftester.sbtbot

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.io.Tcp._
import akka.util.ByteString
import ammonite.ops.{Path, read}
import com.dhpcs.jsonrpc.JsonRpcMessage.{CorrelationId, NoCorrelationId}
import org.perftester.sbtbot.server._
import play.api.libs.json.{JsObject, Json}

import scala.util.{Failure, Success, Try}

object SBTConnActor {

  def props(workspaceRootDir: Path, address: InetSocketAddress): Props = {
    Props(new SBTConnActor(workspaceRootDir, address))
  }
}


class SBTConnActor(sbtRootPath: Path, address: InetSocketAddress) extends Actor with ActorLogging {
  override def preStart(): Unit = {

    //    val portFile = sbtRootPath / "project" / "target" / "active.json"
    //    val file = read(portFile)
    //    Try(Json.parse(file)) match {
    //      case Failure(exception) =>
    //        println("FAILED TO READ SBT FILE")
    //        System.exit(1)
    //      case Success(json : JsObject) =>
    //        val uri = json("uri")
    //        val tokenFilePath = json("tokenfilePath")
    //        println("URI = " + uri + "Path = " + tokenFilePath)
    ////        Json.fromJson[JsonRpcMessage](json).fold({ errors =>
    ////          Left(JsonRpcResponseErrorMessage.invalidRequest(JsError(errors),NoCorrelationId))
    ////        }, Right(_))
    //    }
    import Tcp._
    import context.system

    //    val address = new InetSocketAddress("127.0.0.1",5886)
    IO(Tcp) ! Connect(address)

  }

  override def receive: Receive = setupReceive

  var connection: ActorRef = _

  def setupReceive: Receive = {
    case CommandFailed(_: Connect) ⇒
      println("connect failed")
      context stop self

    case c@Connected(remote, local) ⇒
      println("Connected ! ")
      connection = sender()
      connection ! Register(self)
      context.become(activeReceive)
  }

  def activeReceive: Receive = {
    case Received(byteString) =>
      println("Got " + byteString.decodeString("UTF-8"))
      import server._
      val sb = new StringBuilder
      val msg = ServerCommand.write(InitializeParams(1234L, "/a/a", ClientCapabilities()), NoCorrelationId)
      val r = new MessageWriter
      val content = r.writeByteString(msg)
      connection ! Write(content)
      println("wrote contents")
  }


}
