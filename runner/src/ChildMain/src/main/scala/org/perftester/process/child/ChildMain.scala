package org.perftester.process.child

import java.io._
import java.lang.reflect.InvocationTargetException
import java.net.{InetAddress, Socket}
import java.nio.file.Paths

import javax.net.SocketFactory
import org.perftester.process.comms.input._
import org.perftester.process.comms.output.Complete

import scala.collection.mutable
import scala.hacks.NioFile
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.reporters.NoReporter
import scala.tools.nsc.{Global, Settings}
import scala.util.{Failure, Success, Try}

class ChildMainConfig(host: String = "localhost", port: Int)

class ChildMain(val config:ChildMainConfig ) extends Runnable {

  val socket = connect()
  socket.setSendBufferSize(64000)
  val socketOut = socket.getOutputStream
  val oos       = new ObjectOutputStream(socketOut)
  val socketIn  = socket.getInputStream
  val ois       = new ObjectInputStream(socketIn)

  val origIn  = System.in
  val origOut = System.out
  val origErr = System.err

//  System.setErr(new PrintStream(new ConsoleStream(true, origErr, oos)))
//  System.setOut(new PrintStream(new ConsoleStream(false, origOut, oos)))

  val t = new Thread(this)
  t.setPriority(Thread.MAX_PRIORITY)
  t.setDaemon(false)
  t.start()

  val configs = new mutable.HashMap[String, GlobalHolder]()

  def run() {
    try {
      var done = false
      while (!done) {
        val cmd   = read()
        val start = System.nanoTime()
        val res = Try {
          cmd match {
            case run: Run =>
              val cls    = Class.forName(run.className)
              val method = cls.getMethod("main", classOf[Array[String]])
              assert(method ne null)
              try {
                method.invoke(cls, run.args)
              } catch {
                case ite: InvocationTargetException if ite.getCause.isInstanceOf[DontExit] =>
              }
              ()
            case config: ScalacGlobalConfig =>
              val settings = newSettings()

              Option(config.outputDirectory) foreach ( settings.outputDirs.setSingleOutput(_))
              Option(config.classPath) foreach { cp =>
                settings.classpath.append(cp.mkString(File.pathSeparator))
              }
              Option(config.otherParams) foreach { params =>
                println(s"process args ")
                settings.processArguments(params.toList, processAll = true)
              }

              val holder = new GlobalHolder(new Global(settings, NoReporter))

              configs(config.id) = holder
              Option(config.files) foreach {c => holder.replaceFiles(c.toList)}

            case s: ScalacRun =>
              val holder = configs(s.id)
              val run    = new holder.global.Run()
              //              run.compileSources(holder.sourceFiles)
              run.compile(holder.rawFiles)
            case s: ScalacRetire =>
              configs.remove(s.id).get
              ()
            case _: Gc =>
              System.gc()
              System.runFinalization()
            case _: Exit =>
              done = true
          }
        }
        val duration = System.nanoTime() - start
        println(s"cmd $cmd")
        println(s"duration $duration")
        println(s"res $res")
        val result = res match {
          case Success(a) =>
            new Complete(cmd, duration, false, "")
          case Failure(f) =>
            val sw = new StringWriter()
            val pw = new PrintWriter(sw)
            f.printStackTrace(pw)

            f.printStackTrace

            new Complete(cmd, duration, true, sw.toString)
        }
result.writeTo(oos)
      }
    } catch {
      case t: Throwable =>
        t.printStackTrace()
    }
    SecMan.exit = true
    oos.flush()
    Thread.sleep(1000)
    socket.close()
    System.exit(0)
  }
  def read(): InputCommand = {
    ois.readObject().asInstanceOf[InputCommand]
  }
  def connect(): Socket = {
    SocketFactory.getDefault.createSocket(config.host, config.port)
  }

  class GlobalHolder(val global: Global) {
    private var files: List[BatchSourceFile] = Nil
    var rawFiles: List[String]               = Nil

    def replaceFiles(newFiles: List[String]): Unit = {
      files = newFiles map { n =>
        new BatchSourceFile(NioFile(Paths.get(n)))
      }
      rawFiles = newFiles
    }
    def sourceFiles = files
  }
  def newSettings(): Settings = new Settings(msg => throw new RuntimeException(s"[ERROR] $msg"))
}
//ensure the close doesnt overtake

class DontExit extends AssertionError


//class ConsoleStream(err: Boolean, original: PrintStream, stream: ObjectOutputStream)
//    extends OutputStream {
//
//  val copy = true
//
//  override def write(b: Int): Unit = {
//    new Console(err, new Array[Byte](b.toInt)).writeTo(stream)
//    if (copy) original.write(b)
//  }
//
//  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
//    new Console(err, util.Arrays.copyOfRange(b, off, off + len)).writeTo(stream)
//    if (copy) original.write(b, off, len)
//  }
//}
