package org.perftester.process

import java.io.File
import java.util

object ProcessTest extends App {
  val parent = new Parent(
    ProcessConfiguration(new File("."),
                         None,
                         System.getProperty("java.class.path").split(File.pathSeparator).toList,
                         Nil))

  parent.doRun("org.perftester.process.TestA", "foo", "bar")
  parent.doGc()
  parent.doRun("org.perftester.process.TestB")
}

object TestA extends App {
  println(args.length)
  println(args(0))
  println(args(1))
}

object TestB extends App {
  throw new Exception("boo")
}
