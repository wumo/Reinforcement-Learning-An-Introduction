@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model

import lab.mars.rl.util.*

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
        private val state_function: ((IntSlice) -> Any) -> RandomAccessCollection<Any>,
        private val state_action_function: ((IntSlice) -> Any) -> RandomAccessCollection<Any>) {
    /**
     * 创建由[State]索引的state function
     */
    fun <T : Any> stateFunc(element_maker: (IntSlice) -> Any): RandomAccessCollection<T> {
        return state_function(element_maker) as RandomAccessCollection<T>
    }

    /**
     * 创建由[State]和[Action]索引的state action function
     */
    fun <T : Any> stateActionFunc(element_maker: (IntSlice) -> Any): RandomAccessCollection<T> {
        return state_action_function(element_maker) as RandomAccessCollection<T>
    }
}

class State(val index: IntSlice) : Index {
    inline override val size: Int
        get() = index.size

    inline override operator fun get(dim: Int) = index[dim]

    var actions: RandomAccessCollection<Action> = emptyActions

    override fun toString() = index.toString()
}

class Action(val index: IntSlice) : Index {
    inline override val size: Int
        get() = index.size

    inline override operator fun get(dim: Int) = index[dim]

    var possibles: RandomAccessCollection<Possible> = emptyPossibles

    lateinit var sample: () -> Possible

    override fun toString() = index.toString()
}

class Possible(var next: State, var reward: Double, var probability: Double)

val null_index = DefaultIntSlice.of(-1)
val null_state = State(null_index)
val null_action = Action(null_index)
val null_possible = Possible(null_state, 0.0, 0.0)

val emptyActions = object : RandomAccessCollection<Action>() {
    override fun <T : Any> copycat(element_maker: (IntSlice) -> T): RandomAccessCollection<T> {
        return this as RandomAccessCollection<T>
    }

    override fun <T : Any> _get(idx: Index): T {
        throw IndexOutOfBoundsException()
    }

    override fun <T : Any> _set(idx: Index, s: T) {
        throw IndexOutOfBoundsException()
    }

    override fun indices(): Iterator<IntSlice> = emptyIterator()

    override fun withIndices(): Iterator<Pair<out IntSlice, Action>> = emptyIterator()


    override fun iterator(): Iterator<Action> = emptyIterator()

    override fun ifAny(block: RandomAccessCollection<Action>.() -> Unit) {}
    override fun isEmpty() = true
}

val emptyPossibles = object : RandomAccessCollection<Possible>() {
    override fun <T : Any> copycat(element_maker: (IntSlice) -> T): RandomAccessCollection<T> {
        return this as RandomAccessCollection<T>
    }

    override fun <T : Any> _get(idx: Index): T {
        throw IndexOutOfBoundsException()
    }

    override fun <T : Any> _set(idx: Index, s: T) {
        throw IndexOutOfBoundsException()
    }

    override fun indices(): Iterator<IntSlice> = emptyIterator()

    override fun withIndices(): Iterator<Pair<out IntSlice, Possible>> = emptyIterator()

    override fun iterator(): Iterator<Possible> = emptyIterator()

    override fun ifAny(block: RandomAccessCollection<Possible>.() -> Unit) {}
    override fun isEmpty() = true
}

fun <T> emptyIterator() = object : Iterator<T> {
    override fun hasNext() = false

    override fun next() = throw IndexOutOfBoundsException()
}