@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model

import lab.mars.rl.util.buf.*
import lab.mars.rl.util.collection.IndexedCollection
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.math.Rand
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
typealias DeterminedPolicy = IndexedCollection<IndexedAction>
typealias NonDeterminedPolicy = IndexedCollection<Double>
typealias OptimalSolution = tuple3<NonDeterminedPolicy, StateValueFunction, ActionValueFunction>


interface MDP {
    val γ: Double
    val started: () -> State
}

/**
 *
 * @property states 状态集
 * @property γ 衰减因子
 * @property state_function 状态V函数的构造器（不同的[states]实现对应着不同的[state_function]）
 * @property state_action_function 状态动作Q函数的构造器（不同的[states]和[IndexedAction]实现实现对应着不同的[state_action_function]）
 */
class IndexedMDP(
        val γ: Double,
        val states: StateSet,
        private val state_function: ((Index) -> Any) -> IndexedCollection<Any>,
        private val state_action_function: ((Index) -> Any) -> IndexedCollection<Any>) {
    var started = states
    /**
     * 创建由[IndexedState]索引的state function
     *
     * create state function indexed by [IndexedState]
     */
    fun <T : Any> VFunc(element_maker: (Index) -> T) =
            state_function(element_maker) as IndexedCollection<T>

    /**
     * 创建由[IndexedState]和[IndexedAction]索引的state action function
     *
     * create state action function indexed by [IndexedState] and [IndexedAction]
     */
    fun <T : Any> QFunc(element_maker: (Index) -> T) =
            state_action_function(element_maker) as IndexedCollection<T>

    /**
     * equiprobable random policy
     */
    fun equiprobablePolicy(): NonDeterminedPolicy {
        val policy = QFunc { 0.0 }
        for (s in states) {
            if (s.isTerminal()) continue
            val prob = 1.0 / s.actions.size
            for (a in s.actions)
                policy[s, a] = prob
        }
        return policy
    }
}

interface State {
    var actions: ActionSet
}

inline fun State.isTerminal() = actions.isEmpty()
inline fun State.isNotTerminal() = !actions.isEmpty()

class IndexedState(val index: IntBuf) : Index(), State {
    inline override val size: Int
        get() = index.size

    inline override operator fun get(idx: Int) = index[idx]

    override var actions: ActionSet = emptyNSet as ActionSet
}

interface Action<S : State> {
    var sample: () -> Possible<S>
}

class IndexedAction(val index: IntBuf) : Index(), Action<IndexedState> {
    inline override val size: Int
        get() = index.size

    inline override operator fun get(idx: Int) = index[idx]

    var possibles: PossibleSet = emptyNSet as PossibleSet

    override var sample: () -> Possible<IndexedState> = outer@ {
        if (possibles.isEmpty()) throw NoSuchElementException()
        val p = Rand().nextDouble()
        var acc = 0.0
        for (possible in possibles) {
            acc += possible.probability
            if (p <= acc)
                return@outer possible
        }
        throw IllegalArgumentException("random=$p, but accumulation=$acc")
    }
}

open class Possible<out S : State>(val next: S, val reward: Double) {
    open operator fun component1() = next
    open operator fun component2() = reward
}

class IndexedPossible(next: IndexedState, reward: Double, var probability: Double) : Possible<IndexedState>(next, reward) {
    override operator fun component1() = next
    override operator fun component2() = reward
    operator fun component3() = probability
}

val null_index = DefaultIntBuf.of(-1)
val null_state = IndexedState(null_index)
val null_action = IndexedAction(null_index)
val null_possible = IndexedPossible(null_state, 0.0, 0.0)
val emptyPossibleSet = emptyNSet as PossibleSet
