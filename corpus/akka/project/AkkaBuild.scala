/**
 * Copyright (C) 2009-2017 Lightbend Inc. <http://www.lightbend.com>
 */

package akka

import java.io.{ FileInputStream, InputStreamReader }
import java.util.Properties

import sbt.Keys._
import sbt._
import scala.collection.breakOut

object AkkaBuild {

  val enableMiMa = true

  lazy val buildSettings = Dependencies.Versions ++ Seq(
    organization := "com.typesafe.akka",
    version := "2.5-SNAPSHOT")

  private def allWarnings: Boolean = System.getProperty("akka.allwarnings", "false").toBoolean

  lazy val defaultSettings =
    Seq[Setting[_]](
      // compile options
      scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.8", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
      scalacOptions in Compile ++= (if (allWarnings) Seq("-deprecation") else Nil),
      // -XDignore.symbol.file suppresses sun.misc.Unsafe warnings
      javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-XDignore.symbol.file"),
      javacOptions in compile ++= (if (allWarnings) Seq("-Xlint:deprecation") else Nil),
      javacOptions in doc ++= Seq(),

      crossVersion := CrossVersion.binary,

      ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet,

      licenses := Seq(("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
      homepage := Some(url("http://akka.io/")),

      apiURL := Some(url(s"http://doc.akka.io/api/akka/${version.value}"))
  )

  def loadSystemProperties(fileName: String): Unit = {
    import scala.collection.JavaConverters._
    val file = new File(fileName)
    if (file.exists()) {
      println("Loading system properties from file `" + fileName + "`")
      val in = new InputStreamReader(new FileInputStream(file), "UTF-8")
      val props = new Properties
      props.load(in)
      in.close()
      sys.props ++ props.asScala
    }
  }

  def majorMinor(version: String): Option[String] = """\d+\.\d+""".r.findFirstIn(version)
}
