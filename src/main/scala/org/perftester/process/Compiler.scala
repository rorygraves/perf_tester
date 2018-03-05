package org.perftester.process

import java.io.File
import java.nio.file.Paths

object Compiler extends App {
  val lib = "C:\\Users\\dev\\scalacBuildCache\\d1b745c2e97cc89e5d26b8f5a5696a2611c01af7\\lib\\"
  val classPath =
    s"${lib}jline.jar;${lib}scala-compiler-doc.jar;${lib}scala-compiler.jar;${lib}scala-library.jar;${lib}scala-reflect.jar;${lib}scala-repl-jline-embedded.jar;${lib}scala-repl-jline.jar;${lib}scala-swing_2.12-2.0.0.jar;${lib}scala-xml_2.12-1.0.6.jar;${lib}scalap.jar"
  val params = List("-Xmx10G",
                    "-Xms32M",
                    s"""-Dscala.home="$lib\\.."""",
                    """-Denv.emacs="" """,
                    "-Dscala.usejavacp=true")
  val files = IO.listSourcesIn(Paths.get("S:/scala/akka/akka-actor/src/main/scala")) map (_.toString)

  val compileClassPath = List(
    "C:\\Users\\dev\\.ivy2\\cache\\org.scala-lang.modules\\scala-java8-compat_2.12\\bundles\\scala-java8-compat_2.12-0.8.0.jar",
    "C:\\Users\\dev\\.ivy2\\cache\\com.typesafe\\config\\bundles\\config-1.3.1.jar",
    "C:\\Users\\dev\\.m2\\repository\\com\\typesafe\\akka\\akka-actor_2.12\\2.5.1\\akka-actor_2.12-2.5.1.jar"
  )

  def otherParams(i: Int) =
    List(
      "-sourcepath",
      "S:\\scala\\akka\\akka-actor\\src\\main\\java;S:\\scala\\akka\\akka-actor\\src\\main\\scala",
      "-Yprofile-destination",
      s"S:\\scala\\test\\results\\mike\\COMPILER\\${i}_run_new3.csv"
    )

//  def allParams(i: Int) =
//    List(
//      "-d",
//      "z:",
//      "-classpath",
//      "C:\\Users\\dev\\.ivy2\\cache\\org.scala-lang.modules\\scala-java8-compat_2.12\\bundles\\scala-java8-compat_2.12-0.8.0.jar;C:\\Users\\dev\\.ivy2\\cache\\com.typesafe\\config\\bundles\\config-1.3.1.jar;C:\\Users\\dev\\.m2\\repository\\com\\typesafe\\akka\\akka-actor_2.12\\2.5.1\\akka-actor_2.12-2.5.1.jar",
//      "-sourcepath",
//      "S:\\scala\\akka\\akka-actor\\src\\main\\java;S:\\scala\\akka\\akka-actor\\src\\main\\scala",
//      "-Yprofile-destination",
//      s"S:\\scala\\test\\results\\mike\\COMPILER\\${i}_run_new2.csv"
//    ) ++ files
//
  for (vm <- 1 to 20) {
    val parent = new Parent(
      new ProcessConfiguration(new File("."), None, classPath.split(";").toList, params))
    parent.createGlobal("", "z:", compileClassPath, otherParams(vm), files)
    for (cycle <- 1 to 100) {
      val response = parent.runGlobal("")
      println(s" run $vm # $cycle took ${response / 1000 / 1000.0} ms")

      parent.doGc()
    }
    parent.doExit()
  }

}
