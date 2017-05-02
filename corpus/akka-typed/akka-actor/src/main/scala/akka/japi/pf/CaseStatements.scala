/**
 * Copyright (C) 2009-2017 Lightbend Inc. <http://www.lightbend.com>
 */

package akka.japi.pf

import FI.{ UnitApply, Apply, Predicate }

private[pf] object CaseStatement {
  def empty[F, T](): PartialFunction[F, T] = PartialFunction.empty
}

private[pf] class CaseStatement[-F, +P, T](predicate: Predicate, apply: Apply[P, T])
    extends PartialFunction[F, T] {

  override def isDefinedAt(o: F): Boolean = predicate.defined(o)

  override def apply(o: F): T = apply.apply(o.asInstanceOf[P])
}

private[pf] class UnitCaseStatement[F, P](predicate: Predicate, apply: UnitApply[P])
    extends PartialFunction[F, Unit] {

  override def isDefinedAt(o: F): Boolean = predicate.defined(o)

  override def apply(o: F): Unit = apply.apply(o.asInstanceOf[P])
}
