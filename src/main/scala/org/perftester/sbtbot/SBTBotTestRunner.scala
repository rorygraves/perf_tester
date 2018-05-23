package org.perftester.sbtbot

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.testkit.TestProbe
import ammonite.ops.Path
import org.perftester.sbtbot.SBTBot.{ExecuteTask, SBTBotReady, TaskResult}

object SBTBotTestRunner {

  /**
    * Run a series of repeated sbt commands
    *
    * @param testDir     The test directory
    * @param programArgs The inital sbt commands (e.g. setting flags)
    * @param jvmArgs     The jvm args to append (e.g. to increase memory)
    * @param repeats     The number of times to iterate
    * @param commands    The commands to execute on each iteration.
    */
  def run(testDir: Path,
          programArgs: List[String],
          jvmArgs: List[String],
          repeats: Int,
          commands: List[String],
          debugging: Boolean): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("test")

    val manager = IO(Tcp)

    val proxy = TestProbe()
    val parent = actorSystem.actorOf(Props(new Actor {
      val child: ActorRef =
        context.actorOf(SBTBot.props(testDir, programArgs, jvmArgs), "sbtbot")

      def receive: Receive = {
        case x if sender == child => proxy.ref forward x
        case x                    => child forward x
      }
    }))

    import scala.concurrent.duration._

    try {
      proxy.expectMsg(600.seconds, SBTBotReady)
      println("SBT Bot ready - starting run")
      val timeout = if (debugging) 20 minutes else 40 minutes

      for (i <- 1 to repeats) {
        implicit val sender: ActorRef = proxy.ref
        commands.zipWithIndex foreach {
          case (cmd, idx) =>
            println(
              s"--------------- $cmd - iteration  $i/$repeats -------------------------------")
            parent ! ExecuteTask(s"$idx", cmd)
            proxy.expectMsgClass(timeout, classOf[TaskResult])
//            Thread.sleep(5000)
        }
      }

      println("---------------Finished --------------------------------")
    } finally {
      actorSystem.terminate()
    }
  }
}
