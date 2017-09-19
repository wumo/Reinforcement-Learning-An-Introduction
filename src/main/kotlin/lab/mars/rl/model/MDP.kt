@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

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
typealias DeterminedPolicy = RandomAccessCollection<Action?>
typealias NonDeterminedPolicy = RandomAccessCollection<Double>
/**
 *
 * @property states 状态集
 * @property gamma 衰减因子
 * @property v_maker 状态V函数的构造器（不同的[states]实现对应着不同的[v_maker]）
 * @property q_maker 状态动作Q函数的构造器（不同的[states]和[Action]实现实现对应着不同的[q_maker]）
 * @property pi_maker 策略构造器（不同的[states]实现对应着不同的[pi_maker]
 */
class MDP(
        val gamma: Double,
        val states: StateSet,
        val v_maker: () -> StateValueFunction,
        val q_maker: () -> ActionValueFunction,
        val pi_maker: () -> DeterminedPolicy)

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

val emptyActions = object : RandomAccessCollection<Action>() {
    override fun indices(): Iterator<IntSlice> = emptyIterator()

    override fun withIndices(): Iterator<Pair<out IntSlice, Action>> = emptyIterator()

    override fun <T> _set(idx: Index, s: T) {
        throw IndexOutOfBoundsException()
    }

    override fun <T> _get(idx: Index): T {
        throw IndexOutOfBoundsException()
    }

    override fun iterator(): Iterator<Action> = emptyIterator()
}

val emptyPossibles = object : RandomAccessCollection<Possible>() {
    override fun indices(): Iterator<IntSlice> = emptyIterator()

    override fun withIndices(): Iterator<Pair<out IntSlice, Possible>> = emptyIterator()

    override fun <T> _set(idx: Index, s: T) {
        throw IndexOutOfBoundsException()
    }

    override fun <T> _get(idx: Index): T {
        throw IndexOutOfBoundsException()
    }

    override fun iterator(): Iterator<Possible> = emptyIterator()
}

fun <T> emptyIterator() = object : Iterator<T> {
    override fun hasNext() = false

    override fun next() = throw IndexOutOfBoundsException()
}