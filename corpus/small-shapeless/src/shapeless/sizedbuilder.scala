
/*
 * Copyright (c) 2011-14 Miles Sabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shapeless
  
class SizedBuilder[CC[_]] {
  import scala.collection.Factory
  import nat._
  import Sized.wrap

  def apply[T](a:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _1]((factory.newBuilder += (a)).result)

  def apply[T](a:T, b:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _2]((factory.newBuilder += (a, b)).result)

  def apply[T](a:T, b:T, c:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _3]((factory.newBuilder += (a, b, c)).result)

  def apply[T](a:T, b:T, c:T, d:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _4]((factory.newBuilder += (a, b, c, d)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _5]((factory.newBuilder += (a, b, c, d, e)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _6]((factory.newBuilder += (a, b, c, d, e, f)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _7]((factory.newBuilder += (a, b, c, d, e, f, g)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _8]((factory.newBuilder += (a, b, c, d, e, f, g, h)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _9]((factory.newBuilder += (a, b, c, d, e, f, g, h, i)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _10]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _11]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _12]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _13]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T, n:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _14]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m, n)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T, n:T, o:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _15]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T, n:T, o:T, p:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _16]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T, n:T, o:T, p:T, q:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _17]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T, n:T, o:T, p:T, q:T, r:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _18]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T, n:T, o:T, p:T, q:T, r:T, s:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _19]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T, n:T, o:T, p:T, q:T, r:T, s:T, t:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _20]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T, n:T, o:T, p:T, q:T, r:T, s:T, t:T, u:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _21]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u)).result)

  def apply[T](a:T, b:T, c:T, d:T, e:T, f:T, g:T, h:T, i:T, j:T, k:T, l:T, m:T, n:T, o:T, p:T, q:T, r:T, s:T, t:T, u:T, v:T)
    (implicit dis: DefaultToIndexedSeq[CC], factory : Factory[T, CC[T]], ev : AdditiveCollection[CC[T]]) =
    wrap[CC[T], _22]((factory.newBuilder += (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v)).result)

}