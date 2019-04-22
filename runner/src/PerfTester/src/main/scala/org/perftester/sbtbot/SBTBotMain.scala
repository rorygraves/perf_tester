package org.perftester.sbtbot

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import SBTBot.{ExecuteTask, SBTBotReady, TaskResult}

// Test class for SBTBot
object SBTBotMain {

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("test")
    import ammonite.ops._

    val proxy = TestProbe()
    val parent = actorSystem.actorOf(Props(new Actor {
      val child: ActorRef =
        context.actorOf(SBTBot.props(root / "workspace" / "perf_tester" / "corpus" / "akka",
                                     List.empty,
                                     List.empty),
                        "sbtbot")

      def receive: Receive = {
        case x if sender == child => proxy.ref forward x
        case x                    => child forward x
      }
    }))

    import scala.concurrent.duration._

    try {
      proxy.expectMsg(600.seconds, SBTBotReady)
      println("SBT Bot ready - triggering clean")

      val testName = "ActionCompositionSpec"

      implicit val sender: ActorRef = proxy.ref
      println("---------------clean--------------------------------")
      parent ! ExecuteTask("1", "clean")
      proxy.expectMsgClass(30.seconds, classOf[TaskResult])

      println("---------------compile--------------------------------")
      parent ! ExecuteTask("2", "compile")
      proxy.expectMsgClass(60.seconds, classOf[TaskResult])

      println("---------------Finished --------------------------------")
      println("Finished")
    } finally {
      actorSystem.terminate()
    }
  }
}
