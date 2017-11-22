@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model

import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.collection.*
import lab.mars.rl.util.tuples.tuple3

/**
 * <p>
 * Created on 2017-08-31.
 * </p>
 *
 * @author wumo
 */
typealias StateSet = IndexedCollection<IndexedState>
typealias ActionSet = IndexedCollection<IndexedAction>
typealias PossibleSet = IndexedCollection<IndexedPossible>
typealias StateValueFunction = IndexedCollection<Double>
typealias ActionValueFunction = IndexedCollection<Double>
typealias OptimalSolution = tuple3<IndexedPolicy, StateValueFunction, ActionValueFunction>

interface MDP {
    val γ: Double
    val started: RandomGettable<State>
}

interface Policy {
    operator fun invoke(s: State): Action<State>
    operator fun get(s: State, a: Action<State>): Double
    fun `ε-greedy update`(s: State, evaluate: Gettable<Action<State>, Double>, ε: Double = 0.1)
}

interface State {
    var actions: ActionSet
}

inline fun State.isTerminal() = actions.isEmpty()
inline fun State.isNotTerminal() = !actions.isEmpty()

interface Action<out S : State> {
    val sample: () -> Possible<S>
}

open class Possible<out S : State>(val next: S, val reward: Double) {
    open operator fun component1() = next
    open operator fun component2() = reward
}

val null_index = DefaultIntBuf.of(-1)
val null_state = IndexedState(null_index)
val null_action = IndexedAction(null_index)
val null_possible = IndexedPossible(null_state, 0.0, 0.0)
val emptyPossibleSet = emptyNSet as PossibleSet
