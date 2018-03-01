package org.perftester.process

import java.io.{File, ObjectInputStream, ObjectOutputStream}
import java.lang.reflect.InvocationTargetException
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.util.{Failure, Success}

class Parent(directory: File,
             environment: Option[Map[String, String]],
             classPath: List[String],
             params: List[String]) {
  private val server = new ServerSocket(0)

  private val port = server.getLocalPort

  private val builder = new ProcessBuilder().directory(directory)

  environment foreach { env =>
    val e = builder.environment()
    e.clear()
    env foreach {
      case (k, v) => e.put(k, v)
    }
  }

  val fullClassPath =
    (getClass.getProtectionDomain.getCodeSource.getLocation.getFile.toString :: classPath)
      .mkString("", File.pathSeparator, "")

  builder.inheritIO()
  val allParams = List("java", "-classpath", fullClassPath) ++ params ++ List(
    "org.perftester.process.ChildMain",
    port.toString)

  println(allParams.mkString(" "))
  builder.command(allParams: _*)

  private val process = builder.start()
  server.setSoTimeout(10000) //10 seconds
  private val socket = server.accept()
  server.close()
  private val out = socket.getOutputStream
  private val oos = new ObjectOutputStream(out)
  private val in = socket.getInputStream
  private val ois = new ObjectInputStream(in)

  private val t = new Thread(new Runnable {
    override def run(): Unit = {
      while (true) {
        val res = ois.readObject().asInstanceOf[Outputs]
        res match {
          case Console(err, data) =>
            val s = new String(data)
            (if (err) System.err else System.out).println(s"Console -- $s")
          case c: Complete =>
            response.success(c)
        }
      }
    }
  })
  t.start()

  var maxDuration = 10 minutes
  private val closed = new AtomicBoolean()

  private var response: Promise[Complete] = _

  def exec(cmd: Inputs, duration: Duration) = synchronized {
    response = Promise[Complete]
    cmd.writeTo(oos)
    println(s"running $cmd")
    Await.result(response.future, duration) match {
      case Complete(c, r) if c == cmd =>
        r match {
          case Success(()) =>
          case Failure(t) => throw new InvocationTargetException(t)
        }
    }

  }

  def doRun(className: String, args: String*) = {
    exec(Run(className, args), maxDuration)
  }

  def doGc() = {
    exec(Gc, 1 minute)
  }

  def doExit() = {
    exec(Exit, 1 minute)
    closed.set(true)
    socket.close()
  }

}
