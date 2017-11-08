@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model

import lab.mars.rl.util.Index
import lab.mars.rl.util.Rand
import lab.mars.rl.util.RandomAccessCollection
import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.buf.IntBuf
import lab.mars.rl.util.emptyNSet
import lab.mars.rl.util.tuples.tuple3

/**
 * <p>
 * Created on 2017-08-31.
 * </p>
 *
 * @author wumo
 */
typealias StateSet = RandomAccessCollection<State>
typealias ActionSet = RandomAccessCollection<Action>
typealias PossibleSet = RandomAccessCollection<Possible>
typealias StateValueFunction = RandomAccessCollection<Double>
typealias ActionValueFunction = RandomAccessCollection<Double>
typealias DeterminedPolicy = RandomAccessCollection<Action>
typealias NonDeterminedPolicy = RandomAccessCollection<Double>
typealias OptimalSolution = tuple3<NonDeterminedPolicy, StateValueFunction, ActionValueFunction>
/**
 *
 * @property states 状态集
 * @property gamma 衰减因子
 * @property state_function 状态V函数的构造器（不同的[states]实现对应着不同的[state_function]）
 * @property state_action_function 状态动作Q函数的构造器（不同的[states]和[Action]实现实现对应着不同的[state_action_function]）
 */
class MDP(
        val gamma: Double,
        val states: StateSet,
        private val state_function: ((Index) -> Any) -> RandomAccessCollection<Any>,
        private val state_action_function: ((Index) -> Any) -> RandomAccessCollection<Any>) {
    var started = states
    /**
     * 创建由[State]索引的state function
     *
     * create state function indexed by [State]
     */
    fun <T : Any> VFunc(element_maker: (Index) -> T): RandomAccessCollection<T> {
        return state_function(element_maker) as RandomAccessCollection<T>
    }

    /**
     * 创建由[State]和[Action]索引的state action function
     *
     * create state action function indexed by [State] and [Action]
     */
    fun <T : Any> QFunc(element_maker: (Index) -> T): RandomAccessCollection<T> {
        return state_action_function(element_maker) as RandomAccessCollection<T>
    }

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

class State(val index: IntBuf) : Index() {
    inline override val size: Int
        get() = index.size

    inline override operator fun get(idx: Int) = index[idx]

    var actions: ActionSet = emptyNSet as ActionSet

    inline fun isTerminal() = actions.isEmpty()
    inline fun isNotTerminal() = !actions.isEmpty()
}

class Action(val index: IntBuf) : Index() {
    inline override val size: Int
        get() = index.size

    inline override operator fun get(idx: Int) = index[idx]

    var possibles: PossibleSet = emptyNSet as PossibleSet

    var sample: () -> Possible = outer@ {
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

class Possible(var next: State, var reward: Double, var probability: Double) {
    operator fun component1() = next
    operator fun component2() = reward
    operator fun component3() = probability
}

val null_index = DefaultIntBuf.of(-1)
val null_state = State(null_index)
val null_action = Action(null_index)
val null_possible = Possible(null_state, 0.0, 0.0)

