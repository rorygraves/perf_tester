package org.perftester.process

import java.io.{File, ObjectInputStream, ObjectOutputStream}
import java.net.{ServerSocket, SocketException}
import java.util.concurrent.atomic.AtomicBoolean

import org.perftester.process.comms.input._
import org.perftester.process.comms.output._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

case class ProcessConfiguration(directory: File,
                                environment: Option[Map[String, String]],
                                classPath: List[String],
                                params: List[String])

class Parent(config: ProcessConfiguration) {
  private val server = new ServerSocket(0)

  private val port = server.getLocalPort

  private val builder = new ProcessBuilder().directory(config.directory)

  config.environment foreach { env =>
    val e = builder.environment()
    e.clear()
    env foreach {
      case (k, v) => e.put(k, v)
    }
  }

  val fullClassPath =
    config.classPath.mkString("", File.pathSeparator, "")

  builder.inheritIO()
  val allParams = List("java", "-classpath", fullClassPath) ++ config.params ++ List(
    "org.perftester.process.child.Bootstrap",
    port.toString)

  println(allParams.mkString(" "))
  builder.command(allParams: _*)

  private val process = builder.start()
  server.setSoTimeout(100000) //10 seconds
  private val socket = server.accept()
  server.close()
  private val out = socket.getOutputStream
  private val oos = new ObjectOutputStream(out)
  private val in  = socket.getInputStream
  private val ois = new ObjectInputStream(in)

  private val t = new Thread(new Runnable {
    override def run(): Unit = {
      while (!socket.isClosed && !socket.isInputShutdown) {
        try {
          val res = ois.readObject().asInstanceOf[OutputCommand]
          res match {
            case c: ConsoleOutput=>
              val s = new String(c.text)
              (if (c.err) System.err else System.out).println(s"Console -- $s")
            case c: Complete =>
              response.success(c)
          }
        } catch {
          case e: SocketException
              if (e.toString contains "Connection reset") || socket.isInputShutdown =>
            response.tryFailure(e)
            socket.close()
          case NonFatal(t) =>
            t.printStackTrace()
            response.tryFailure(t)
            socket.close()
        }
      }
    }
  })
  t.start()

  var maxDuration    = 10 minutes
  private val closed = new AtomicBoolean()

  private var response: Promise[Complete] = _

  def exec(cmd: InputCommand, duration: Duration) = synchronized {
    response = Promise[Complete]
    cmd.writeTo(oos)
    println(s"sending $cmd")
    Await.result(response.future, duration) match {
      case cmp: Complete if cmp.input == cmd =>
        if (cmp.failed) {
          System.err.print(cmp.failMessage)
          throw new Exception()
        } else cmp.duration
    }

  }

  def doRun(className: String, args: String*) = {
    exec(new Run(className, args.toArray), maxDuration)
  }

  def createGlobal(id: String,
                   outputDirectory: String,
                   classPath: Seq[String],
                   otherParams: List[String],
                   files: List[String]) = {
    exec(new ScalacGlobalConfig(id,
                                outputDirectory,
                                classPath.toArray,
                                otherParams.toArray,
                                files.toArray),
         maxDuration)
  }
  def destroyGlobal(id: String) = {
    exec(new ScalacRetire(id), maxDuration)
  }
  def updateGlobal(id: String,
                   outputDirectory: Option[String],
                   classPath: Option[List[String]],
                   otherParams: Option[List[String]],
                   files: Option[List[String]]) = {
    exec(
      new ScalacGlobalConfig(
        id,
        outputDirectory.orNull,
        (classPath.map (_.toArray)).orNull,
        (otherParams.map (_.toArray)).orNull,
        (files.map (_.toArray)).orNull
      ),
      maxDuration
    )
  }
  def runGlobal(id: String) = {
    exec(new ScalacRun(id), maxDuration)
  }

  def doGc() = {
    exec(new Gc, 1 minute)
  }

  def doExit() = {
    exec(new Exit, 1 minute)
    closed.set(true)
    socket.close()
  }

}
