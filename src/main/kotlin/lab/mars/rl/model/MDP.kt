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
    override fun get(idx: Index): Action {
        throw Exception()
    }

    override fun set(idx: Index, s: Action) {
        throw Exception()
    }

    override fun iterator(): Iterator<Action> = object : Iterator<Action> {
        override fun hasNext() = false

        override fun next() = throw Exception()
    }
}

val emptyPossibles = object : RandomAccessCollection<Possible>() {
    override fun get(idx: Index): Possible {
        throw Exception()
    }

    override fun set(idx: Index, s: Possible) {
        throw Exception()
    }

    override fun iterator(): Iterator<Possible> = object : Iterator<Possible> {
        override fun hasNext() = false

        override fun next() = throw Exception()
    }
}