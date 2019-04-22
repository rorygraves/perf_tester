package org.perfagent.tests

import org.perfagent.CollectionTraceSupport

object testCollection extends App {

  val list: List[Int] = (1 to 5).toList

  list foreach println

  val statistic = CollectionTraceSupport.getStatisticAndReset()
  val callerStatistic = CollectionTraceSupport.getCallerStatisticAndReset
  val ctorStatistic = CollectionTraceSupport.getCtorStatisticAndReset

  publish("normal", statistic)
  publish("caller", callerStatistic)
  publish("ctor", ctorStatistic)

  import java.lang. { String => JString, Long => JLong }
  import java.util
  import scala.collection.JavaConverters._
  def publish(name: String, result: util.HashMap[JString, JLong]): Unit = {
    println(s"result for ${name}")
    result.asScala foreach { case (k, v) =>
      println(s"method name is: ${k}, count is: ${v}")
    }
  }
}
