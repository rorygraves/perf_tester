package org.perftester.process

import java.io._
import java.lang.reflect.InvocationTargetException
import java.net.{InetAddress, Socket}
import java.security.Permission
import java.util

import javax.net.SocketFactory
import scopt.OptionParser

import scala.util.Try

//object ChildMainParser extends OptionParser[ChildMainConfig]("ChildMain") {
//  val defaults = ChildMainConfig(port = -1)
//  head("Child", "1.0")
//
//  opt[String]("parentHost")
//    .action((x, c) => c.copy(host = x))
//    .text(s"The host to connect to")
//
//  opt[Int]("parentPort")
//    .action((x, c) => c.copy(port = x))
//    .text(s"The port to connect to")
//}
//
case class ChildMainConfig(host: String = "localhost", port: Int)

object ChildMain extends App with Runnable {
  val cmd = ChildMainConfig(port = args(0).toInt)

  System.setSecurityManager(SecMan)

  val socket = connect()
  socket.setSendBufferSize(64000)
  val socketOut = socket.getOutputStream
  val oos       = new ObjectOutputStream(socketOut)
  val socketIn  = socket.getInputStream
  val ois       = new ObjectInputStream(socketIn)

  val origIn  = System.in
  val origOut = System.out
  val origErr = System.err

  System.setErr(new PrintStream(new ConsoleStream(true, origErr, oos)))
  System.setOut(new PrintStream(new ConsoleStream(false, origOut, oos)))

  val t = new Thread(this)
  t.setPriority(Thread.MAX_PRIORITY)
  t.setDaemon(false)
  t.start()

  def run() {
    try {
      var done = false
      while (!done) {
        val cmd = read()
        val res = Try {
          cmd match {
            case Run(className, params) =>
              val cls    = Class.forName(className)
              val method = cls.getMethod("main", classOf[Array[String]])
              assert(method ne null)
              try {
                method.invoke(cls, params.toArray)
              } catch {
                case ite: InvocationTargetException if ite.getCause.isInstanceOf[DontExit] =>
              }
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
    SecMan.exit = true
    oos.flush()
    //ensure the close doesnt overtake
    Thread.sleep(1000)
    socket.close()
    System.exit(0)
  }
  def read(): Inputs = {
    ois.readObject().asInstanceOf[Inputs]
  }

  def connect(): Socket = {
    SocketFactory.getDefault.createSocket(cmd.host, cmd.port)
  }

}

class DontExit extends AssertionError

object SecMan extends SecurityManager {
  var exit = false

  override def checkExit(status: Int): Unit = {
    if (!exit) throw new DontExit
  }

  override def checkExec(cmd: String): Unit = ()

  override def checkAwtEventQueueAccess(): Unit = ()

  override def checkPrintJobAccess(): Unit = ()

  override def checkMulticast(maddr: InetAddress): Unit = ()

  override def checkMulticast(maddr: InetAddress, ttl: Byte): Unit = ()

  override def checkPermission(perm: Permission): Unit = ()

  override def checkPermission(perm: Permission, context: scala.Any): Unit = ()

  override def checkAccept(host: String, port: Int): Unit = ()

  override def checkSetFactory(): Unit = ()

  override def checkLink(lib: String): Unit = ()

  override def checkWrite(fd: FileDescriptor): Unit = ()

  override def checkWrite(file: String): Unit = ()

  override def checkPropertyAccess(key: String): Unit = ()

  override def checkSecurityAccess(target: String): Unit = ()

  override def checkListen(port: Int): Unit = ()

  override def checkAccess(t: Thread): Unit = ()

  override def checkAccess(g: ThreadGroup): Unit = ()

  override def checkDelete(file: String): Unit = ()

  override def checkCreateClassLoader(): Unit = ()

  override def checkPackageDefinition(pkg: String): Unit = ()

  override def checkConnect(host: String, port: Int): Unit = ()

  override def checkConnect(host: String, port: Int, context: scala.Any): Unit = ()

  override def checkPackageAccess(pkg: String): Unit = ()

  override def checkPropertiesAccess(): Unit = ()

  override def checkSystemClipboardAccess(): Unit = ()

  override def checkRead(fd: FileDescriptor): Unit = ()

  override def checkRead(file: String): Unit = ()

  override def checkRead(file: String, context: scala.Any): Unit = ()
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
