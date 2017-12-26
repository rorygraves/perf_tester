package org.perftester.sbtbot

import java.net.InetSocketAddress

import akka.actor
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.testkit.TestProbe
import org.perftester.sbtbot.SBTBot.{ExecuteTask, SBTBotReady, TaskResult}

object SBTConnActorTestMain {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("test")
    import ammonite.ops._


    val manager = IO(Tcp)

    val proxy = TestProbe()
    val parent = actorSystem.actorOf(Props(new Actor {
      val child: ActorRef = context.actorOf(SBTConnActor.props(root / "workspace" / "akka",new InetSocketAddress("127.0.0.1",5886)), "sbtbot")

      def receive: Receive = {
        case x if sender == child => proxy.ref forward x
        case x => child forward x
      }
    }))

    import scala.concurrent.duration._

    try {
      proxy.expectMsg(10.seconds, SBTBotReady)
      println("SBT Bot ready - triggering clean")

//      val testName = "ActionCompositionSpec"
//
//      implicit val sender: ActorRef = proxy.ref
//      println("---------------clean--------------------------------")
//      parent ! ExecuteTask("1", "clean")
//      proxy.expectMsgClass(30.seconds, classOf[TaskResult])
//
//      println("---------------compile--------------------------------")
//      parent ! ExecuteTask("2", "compile")
//      proxy.expectMsgClass(60.seconds, classOf[TaskResult])
//
//
//      println("---------------Finished --------------------------------")
      println("Finished")
    } finally {
      actorSystem.terminate()
    }
  }
}

