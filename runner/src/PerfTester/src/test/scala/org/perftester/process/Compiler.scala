package org.perftester.process

import java.io.File
import java.nio.file.Paths

object Compiler extends App {
  def toFileString(s: String) = s.replace('/', File.separatorChar)
  val ivy                     = toFileString(s"${System.getProperty("user.home")}/.ivy2/cache")
  val maven                   = toFileString(s"${System.getProperty("user.home")}/.m2/repository")
  val lib = toFileString(
    "S:\\scala\\2.13\\quick1\\build\\pack\\lib\\")
//    "C:/Users/User/scalacBuildCache/2ed9ba053f2fbf5c06f4f0fc64aabec43125998a/lib/")
//    s"$ivy/org.scala-lang/scala-reflect/jars")

//  val root = Paths.get(".").toRealPath()

  val classPath = List(
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\charsets.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\deploy.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\access-bridge-64.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\cldrdata.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\dnsns.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\jaccess.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\jfxrt.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\localedata.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\nashorn.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\sunec.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\sunjce_provider.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\sunmscapi.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\sunpkcs11.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\ext\\zipfs.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\javaws.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\jce.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\jfr.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\jfxswt.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\jsse.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\management-agent.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\plugin.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\resources.jar",
//    "C:\\Program Files\\Java\\jdk1.8.0_144\\jre\\lib\\rt.jar",
//    "S:\\scala\\perf\\tester-base\\perf_tester\\runner\\src\\ChildMain\\target\\scala-2.12\\classes",
//    "S:\\scala\\perf\\tester-base\\perf_tester\\runner\\src\\SharedComms\\target\\classes",
//    "C:\\Users\\User\\.ivy2\\cache\\org.scala-lang.modules\\scala-xml_2.12\\bundles\\scala-xml_2.12-1.0.6.jar",

//    "C:\\Users\\User\\.ivy2\\cache\\org.scala-lang\\scala-reflect\\jars\\scala-reflect-2.12.8.jar",
//    "C:\\Users\\User\\.ivy2\\cache\\org.scala-lang\\scala-library\\jars\\scala-library-2.12.8.jar",
//    "C:\\Users\\User\\.ivy2\\cache\\org.scala-lang\\scala-compiler\\jars\\scala-compiler-2.12.8.jar",

//    "C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2018.3.1\\lib\\idea_rt.jar",

//    s"${lib}jline.jar",
//    s"${lib}scala-compiler-doc.jar",
    s"${lib}scala-compiler.jar",
    s"${lib}scala-library.jar",
    s"${lib}scala-reflect.jar",
//    s"${lib}scala-repl-jline-embedded.jar",
//    s"${lib}scala-repl-jline.jar",
//    s"${lib}scala-swing_2.12-2.0.0.jar",
//    s"${lib}scala-xml_2.12-1.0.6.jar",
//    s"${lib}scalap.jar",
    s"${Paths.get("src", "ChildMain", "target", "scala-2.12", "classes").toAbsolutePath.toRealPath().toString}",
    s"${Paths.get("src", "SharedComms", "target", "classes").toAbsolutePath.toRealPath().toString}"
  )
  classPath foreach println

  val params = List(
    //"-verbose",
                    "-Xmx10G",
                    "-Xms32M",
                    s"""-Dscala.home="$lib\\.."""",
                    """-Denv.emacs="" """,
                    "-Dscala.usejavacp=true")
  private val sourcePath =
    Paths.get("../corpus/akka/akka-actor/src/main/scala").toAbsolutePath.toRealPath()
  val files = IO.listSourcesIn(sourcePath) map (_.toString)

  val compileClassPath = List(
//    toFileString(s"$ivy/org.scala-lang.modules/scala-java8-compat_2.12/bundles/scala-java8-compat_2.12-0.8.0.jar"),
//    toFileString(s"$ivy/com.typesafe/config/bundles/config-1.3.1.jar"),
    toFileString(s"$maven/com/typesafe/akka/akka-actor_2.12/2.5.1/akka-actor_2.12-2.5.1.jar")
  )

  def otherParams(i: Int) =
    List(
      "-sourcepath",
      sourcePath.toString,
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
    val parent = new Parent(new ProcessConfiguration(new File("."), None, classPath, params))
    parent.createGlobal("", "s:\\mike\\", compileClassPath, otherParams(vm), files)
    for (cycle <- 1 to 100) {
      val response = parent.runGlobal("")
      println(s" run $vm # $cycle took ${response / 1000 / 1000.0} ms")

      parent.doGc()
    }
    parent.doExit()
  }

}
