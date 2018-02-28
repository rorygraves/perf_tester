package org.perftester.process

import java.io.{ObjectInputStream, ObjectOutputStream, OutputStream, PrintStream}
import java.net.Socket
import java.util

import javax.net.SocketFactory
import scopt.OptionParser

import scala.util.Try

object ChildMainParser extends OptionParser[ChildMainConfig]("ChildMain") {
  val defaults = ChildMainConfig(port = -1)
  head("Child", "1.0")

  opt[String]("parentHost")
    .action((x, c) => c.copy(host = x))
    .text(s"The host to connect to")

  opt[Int]("parentPort")
    .action((x, c) => c.copy(port = x))
    .text(s"The port to connect to")
}

case class ChildMainConfig(host: String = "localhost", port: Int)

object ChildMain extends App {
  val cmd = ChildMainParser.parse(args, ChildMainParser.defaults).getOrElse(???)

  val socket = connect()
  socket.setSendBufferSize(64000)
  val socketOut = socket.getOutputStream
  val oos = new ObjectOutputStream(socketOut)
  val socketIn = socket.getInputStream
  val ois = new ObjectInputStream(socketIn)

  val origIn = System.in
  val origOut = System.out
  val origErr = System.err

  System.setErr(new PrintStream(new ConsoleStream(true, origErr, oos)))
  System.setOut(new PrintStream(new ConsoleStream(false, origOut, oos)))

  try {
    var done = false
    while (!done) {
      val cmd = read()
      val res = Try {
        cmd match {
          case Run(className, params) =>
            val cls = Class.forName(className)
            val method = cls.getMethod("main", classOf[Array[String]])
            assert(method ne null)
            method.invoke(cls, params.toArray)
            ()
          case Gc =>
            System.gc()
            System.runFinalization()
          case Exit =>
            done = true
        }
      }
      Complete(cmd, res).writeTo(oos)

    }
  } catch {
    case t: Throwable =>
      t.printStackTrace()
  }
  oos.flush()
  //ensure the close doesnt overtake
  Thread.sleep(1000)
  socket.close()

  def read(): Inputs = {
    ois.readObject().asInstanceOf[Inputs]
  }

  def connect(): Socket = {
    SocketFactory.getDefault.createSocket(cmd.host, cmd.port)
  }

}

class ConsoleStream(err: Boolean, original: PrintStream, stream: ObjectOutputStream)
  extends OutputStream {

  val copy = true

  override def write(b: Int): Unit = {
    new Console(err, new Array[Byte](b.toInt)).writeTo(stream)
    if (copy) original.write(b)
  }

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    new Console(err, util.Arrays.copyOfRange(b, off, off + len)).writeTo(stream)
    if (copy) original.write(b, off, len)
  }
}
