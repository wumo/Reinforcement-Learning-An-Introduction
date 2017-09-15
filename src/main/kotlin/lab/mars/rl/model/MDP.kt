@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.model

import lab.mars.rl.util.IntSlice

/**
 * <p>
 * Created on 2017-08-31.
 * </p>
 *
 * @author wumo
 */
typealias StateSet = IndexedCollection<State>
typealias StateValueFunction = IndexedCollection<Double>
typealias ActionValueFunction = IndexedCollection<Double>
typealias DeterminedPolicy = IndexedCollection<Action?>

/**
 *
 * @property states 状态集
 * @property gamma 衰减因子
 * @property v_maker 状态V函数的构造器（不同的[states]实现对应着不同的[v_maker]）
 * @property q_maker 状态动作Q函数的构造器（不同的[states]和[Action]实现实现对应着不同的[q_maker]）
 * @property pi_maker 策略构造器（不同的[states]实现对应着不同的[pi_maker]
 */
class MDP(
        val states: StateSet,
        val gamma: Double,
        val v_maker: () -> StateValueFunction,
        val q_maker: () -> ActionValueFunction,
        val pi_maker: () -> DeterminedPolicy)

interface Indexable {
    val idx: IntSlice
}

fun concat(indexable: Array<out Indexable>): IntSlice {
    val total = indexable.sumBy { it.idx.size }
    val idx = IntArray(total)
    var _offset = 0
    indexable.forEach {
        it.idx.apply {
            System.arraycopy(backed, offset, idx, _offset, size)
            _offset += size
        }
    }
    return IntSlice.reuse(idx)
}

abstract class IndexedCollection<E> : Iterable<E> {
    abstract operator fun get(idx: IntSlice): E
    inline operator fun get(vararg idx: Int): E = get(IntSlice.reuse(idx))
    inline operator fun get(indexable: Indexable): E = get(indexable.idx)
    inline operator fun get(vararg indexable: Indexable): E = get(concat(indexable))

    abstract operator fun set(idx: IntSlice, s: E)

    inline operator fun set(vararg idx: Int, s: E) {
        set(IntSlice.reuse(idx), s)
    }

    inline operator fun set(indexable: Indexable, s: E) {
        set(indexable.idx, s)
    }

    inline operator fun set(vararg indexable: Indexable, s: E) {
        set(concat(indexable), s)
    }
}

/**
 * 如果集合不为空，则执行[block]
 */
inline fun <E> IndexedCollection<E>.ifAny(block: IndexedCollection<E>.() -> Unit) {
    for (element in this) return block()
}

class State(index: IntSlice) : Indexable {
    override val idx = index

    var actions: IndexedCollection<Action> = emptyActions

    override fun toString() = idx.toString()
}

class Action(index: IntSlice) : Indexable {
    override val idx = index

    var possibles: IndexedCollection<Possible> = emptyPossibles

    lateinit var sample: () -> Possible

    override fun toString() = idx.toString()
}

class Possible(var next: State, var reward: Double, var probability: Double)

val emptyActions = object : IndexedCollection<Action>() {
    override fun get(index: IntSlice): Action {
        throw  Exception()
    }

    override fun set(index: IntSlice, s: Action) {
        throw  Exception()
    }

    override fun iterator(): Iterator<Action> = object : Iterator<Action> {
        override fun hasNext() = false

        override fun next() = throw Exception()
    }
}

val emptyPossibles = object : IndexedCollection<Possible>() {
    override fun get(index: IntSlice): Possible {
        throw  Exception()
    }

    override fun set(index: IntSlice, s: Possible) {
        throw  Exception()
    }

    override fun iterator(): Iterator<Possible> = object : Iterator<Possible> {
        override fun hasNext() = false

        override fun next() = throw Exception()
    }
}