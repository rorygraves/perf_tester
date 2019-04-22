//package org.perftester.process.compiler
//
//import scala.reflect.internal.util.Position
//import scala.tools.nsc.reporters.Reporter
//
//object Reporters {
//  object noInfo extends Reporter { // We are ignoring all
//    override protected def info0(pos: Position,
//                                 msg: String,
//                                 severity: this.Severity,
//                                 force: Boolean): Unit = {
//      println(s"[$severity] $pos: $msg")
//    }
//  }
//}
