
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
  


/**
 * Type class witnessing that type `PP` is equal to `FF[A]` for some higher kinded type `FF[_]` and type(s) `A`.
 * 
 * @author Miles Sabin
 */
trait Unpack1[-PP, FF[_], A]

object Unpack1 {
  implicit def unpack[FF[_], A]: Unpack1[FF[A], FF, A] = new Unpack1[FF[A], FF, A] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B]` for some higher kinded type `FF[_, _]` and type(s) `A, B`.
 * 
 * @author Miles Sabin
 */
trait Unpack2[-PP, FF[_, _], A, B]

object Unpack2 {
  implicit def unpack[FF[_, _], A, B]: Unpack2[FF[A, B], FF, A, B] = new Unpack2[FF[A, B], FF, A, B] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C]` for some higher kinded type `FF[_, _, _]` and type(s) `A, B, C`.
 * 
 * @author Miles Sabin
 */
trait Unpack3[-PP, FF[_, _, _], A, B, C]

object Unpack3 {
  implicit def unpack[FF[_, _, _], A, B, C]: Unpack3[FF[A, B, C], FF, A, B, C] = new Unpack3[FF[A, B, C], FF, A, B, C] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D]` for some higher kinded type `FF[_, _, _, _]` and type(s) `A, B, C, D`.
 * 
 * @author Miles Sabin
 */
trait Unpack4[-PP, FF[_, _, _, _], A, B, C, D]

object Unpack4 {
  implicit def unpack[FF[_, _, _, _], A, B, C, D]: Unpack4[FF[A, B, C, D], FF, A, B, C, D] = new Unpack4[FF[A, B, C, D], FF, A, B, C, D] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E]` for some higher kinded type `FF[_, _, _, _, _]` and type(s) `A, B, C, D, E`.
 * 
 * @author Miles Sabin
 */
trait Unpack5[-PP, FF[_, _, _, _, _], A, B, C, D, E]

object Unpack5 {
  implicit def unpack[FF[_, _, _, _, _], A, B, C, D, E]: Unpack5[FF[A, B, C, D, E], FF, A, B, C, D, E] = new Unpack5[FF[A, B, C, D, E], FF, A, B, C, D, E] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F]` for some higher kinded type `FF[_, _, _, _, _, _]` and type(s) `A, B, C, D, E, F`.
 * 
 * @author Miles Sabin
 */
trait Unpack6[-PP, FF[_, _, _, _, _, _], A, B, C, D, E, F]

object Unpack6 {
  implicit def unpack[FF[_, _, _, _, _, _], A, B, C, D, E, F]: Unpack6[FF[A, B, C, D, E, F], FF, A, B, C, D, E, F] = new Unpack6[FF[A, B, C, D, E, F], FF, A, B, C, D, E, F] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G]` for some higher kinded type `FF[_, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G`.
 * 
 * @author Miles Sabin
 */
trait Unpack7[-PP, FF[_, _, _, _, _, _, _], A, B, C, D, E, F, G]

object Unpack7 {
  implicit def unpack[FF[_, _, _, _, _, _, _], A, B, C, D, E, F, G]: Unpack7[FF[A, B, C, D, E, F, G], FF, A, B, C, D, E, F, G] = new Unpack7[FF[A, B, C, D, E, F, G], FF, A, B, C, D, E, F, G] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H]` for some higher kinded type `FF[_, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H`.
 * 
 * @author Miles Sabin
 */
trait Unpack8[-PP, FF[_, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H]

object Unpack8 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H]: Unpack8[FF[A, B, C, D, E, F, G, H], FF, A, B, C, D, E, F, G, H] = new Unpack8[FF[A, B, C, D, E, F, G, H], FF, A, B, C, D, E, F, G, H] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I`.
 * 
 * @author Miles Sabin
 */
trait Unpack9[-PP, FF[_, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I]

object Unpack9 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I]: Unpack9[FF[A, B, C, D, E, F, G, H, I], FF, A, B, C, D, E, F, G, H, I] = new Unpack9[FF[A, B, C, D, E, F, G, H, I], FF, A, B, C, D, E, F, G, H, I] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J`.
 * 
 * @author Miles Sabin
 */
trait Unpack10[-PP, FF[_, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J]

object Unpack10 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J]: Unpack10[FF[A, B, C, D, E, F, G, H, I, J], FF, A, B, C, D, E, F, G, H, I, J] = new Unpack10[FF[A, B, C, D, E, F, G, H, I, J], FF, A, B, C, D, E, F, G, H, I, J] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K`.
 * 
 * @author Miles Sabin
 */
trait Unpack11[-PP, FF[_, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K]

object Unpack11 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K]: Unpack11[FF[A, B, C, D, E, F, G, H, I, J, K], FF, A, B, C, D, E, F, G, H, I, J, K] = new Unpack11[FF[A, B, C, D, E, F, G, H, I, J, K], FF, A, B, C, D, E, F, G, H, I, J, K] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L`.
 * 
 * @author Miles Sabin
 */
trait Unpack12[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L]

object Unpack12 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L]: Unpack12[FF[A, B, C, D, E, F, G, H, I, J, K, L], FF, A, B, C, D, E, F, G, H, I, J, K, L] = new Unpack12[FF[A, B, C, D, E, F, G, H, I, J, K, L], FF, A, B, C, D, E, F, G, H, I, J, K, L] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M`.
 * 
 * @author Miles Sabin
 */
trait Unpack13[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M]

object Unpack13 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M]: Unpack13[FF[A, B, C, D, E, F, G, H, I, J, K, L, M], FF, A, B, C, D, E, F, G, H, I, J, K, L, M] = new Unpack13[FF[A, B, C, D, E, F, G, H, I, J, K, L, M], FF, A, B, C, D, E, F, G, H, I, J, K, L, M] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M, N`.
 * 
 * @author Miles Sabin
 */
trait Unpack14[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N]

object Unpack14 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N]: Unpack14[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N] = new Unpack14[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M, N, O`.
 * 
 * @author Miles Sabin
 */
trait Unpack15[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O]

object Unpack15 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O]: Unpack15[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O] = new Unpack15[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P`.
 * 
 * @author Miles Sabin
 */
trait Unpack16[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P]

object Unpack16 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P]: Unpack16[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P] = new Unpack16[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q`.
 * 
 * @author Miles Sabin
 */
trait Unpack17[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q]

object Unpack17 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q]: Unpack17[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q] = new Unpack17[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R`.
 * 
 * @author Miles Sabin
 */
trait Unpack18[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R]

object Unpack18 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R]: Unpack18[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R] = new Unpack18[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S`.
 * 
 * @author Miles Sabin
 */
trait Unpack19[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S]

object Unpack19 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S]: Unpack19[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S] = new Unpack19[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T`.
 * 
 * @author Miles Sabin
 */
trait Unpack20[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T]

object Unpack20 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T]: Unpack20[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T] = new Unpack20[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U`.
 * 
 * @author Miles Sabin
 */
trait Unpack21[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U]

object Unpack21 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U]: Unpack21[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U] = new Unpack21[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U] {}
}

/**
 * Type class witnessing that type `PP` is equal to `FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V]` for some higher kinded type `FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]` and type(s) `A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V`.
 * 
 * @author Miles Sabin
 */
trait Unpack22[-PP, FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V]

object Unpack22 {
  implicit def unpack[FF[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _], A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V]: Unpack22[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V] = new Unpack22[FF[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V], FF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V] {}
}
