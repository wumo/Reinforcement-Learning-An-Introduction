@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model

import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.collection.emptyNSet

/**
 * <p>
 * Created on 2017-08-31.
 * </p>
 *
 * @author wumo
 */

interface MDP {
  val γ: Double
  val started: () -> State
}

interface Policy {
  /**sample action when in state [s]*/
  operator fun invoke(s: State): Action<State>
  
  /**probability of taking action [a] when in state [s]*/
  operator fun get(s: State, a: Action<State>): Double
}

interface RandomIterable<out E>: Iterable<E> {
  fun rand(): E
  val size: Int
}

interface State {
  val actions: RandomIterable<Action<State>>
}

inline val State.isTerminal
  get() = !isNotTerminal

inline val State.isNotTerminal
  get() = actions.any()

interface Action<out S: State> {
  val sample: () -> Possible<S>
}

open class Possible<out S: State>(val next: S, val reward: Double) {
  open operator fun component1() = next
  open operator fun component2() = reward
}

val null_index = DefaultIntBuf.of(-1)
val null_state = IndexedState(null_index)
val null_action = IndexedAction(null_index)
val null_possible = IndexedPossible(null_state, 0.0, 0.0)
val emptyPossibleSet: PossibleSet = emptyNSet()
