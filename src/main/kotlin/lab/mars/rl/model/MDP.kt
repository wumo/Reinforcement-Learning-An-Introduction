@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model

import lab.mars.rl.util.*
import lab.mars.rl.util.Bufkt.DefaultIntBuf
import lab.mars.rl.util.Bufkt.IntBuf

/**
 * <p>
 * Created on 2017-08-31.
 * </p>
 *
 * @author wumo
 */
typealias StateSet = RandomAccessCollection<State>
typealias StateValueFunction = RandomAccessCollection<Double>
typealias ActionValueFunction = RandomAccessCollection<Double>
typealias DeterminedPolicy = RandomAccessCollection<Action>
typealias NonDeterminedPolicy = RandomAccessCollection<Double>

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
        private val state_function: ((IntBuf) -> Any) -> RandomAccessCollection<Any>,
        private val state_action_function: ((IntBuf) -> Any) -> RandomAccessCollection<Any>) {
    /**
     * 创建由[State]索引的state function
     */
    fun <T : Any> VFunc(element_maker: (IntBuf) -> Any): RandomAccessCollection<T> {
        return state_function(element_maker) as RandomAccessCollection<T>
    }

    /**
     * 创建由[State]和[Action]索引的state action function
     */
    fun <T : Any> QFunc(element_maker: (IntBuf) -> Any): RandomAccessCollection<T> {
        return state_action_function(element_maker) as RandomAccessCollection<T>
    }
}

class State(val index: IntBuf) : Index {
    inline override val size: Int
        get() = index.size

    inline override operator fun get(idx: Int) = index[idx]

    var actions: RandomAccessCollection<Action> = emptyNSet as RandomAccessCollection<Action>

    override fun toString() = index.toString()
}

class Action(val index: IntBuf) : Index {
    inline override val size: Int
        get() = index.size

    inline override operator fun get(idx: Int) = index[idx]

    var possibles: RandomAccessCollection<Possible> = emptyNSet as RandomAccessCollection<Possible>

    lateinit var sample: () -> Possible

    override fun toString() = index.toString()
}

class Possible(var next: State, var reward: Double, var probability: Double)

val null_index = DefaultIntBuf.of(-1)
val null_state = State(null_index)
val null_action = Action(null_index)
val null_possible = Possible(null_state, 0.0, 0.0)

