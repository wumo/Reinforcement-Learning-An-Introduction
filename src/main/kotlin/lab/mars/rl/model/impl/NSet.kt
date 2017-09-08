@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model.impl

import lab.mars.rl.model.*
import java.util.NoSuchElementException

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
inline fun MutableList<Int>.addAll(num: Int, s: Int) = apply { repeat(num) { add(s) } }

inline fun mutableListOf(num: Int, s: Int) = mutableListOf<Int>().addAll(num, s)

fun MutableList<Int>.increment(dim: IntArray) {
    val offset = lastIndex - dim.size + 1
    for (idx in dim.lastIndex downTo 0) {
        val this_idx = offset + idx
        this[this_idx]++
        if (this[this_idx] < dim[idx])
            break
        this[this_idx] = 0
    }
}

class NSet<E> private constructor(private val dim: IntArray, private val stride: IntArray, private val raw: Array<Any?>) :
        IndexedCollection<E> {
    override fun init(maker: (IntArray) -> Any?) {
        val index = mutableListOf(dim.size, 0)
        for (i in 0 until raw.size)
            raw[i] = maker(index.toIntArray()).apply {
                index.increment(dim)
            }
    }

    companion object {
        operator fun <T, S> invoke(shape: NSet<S>, element_maker: (IntArray) -> Any? = { null }): NSet<T> {
            val index = mutableListOf(shape.dim.size, 0)
            val raw = Array(shape.raw.size) {
                copycat<T, S>(shape.raw[it], index, element_maker)
                        .apply { index.increment(shape.dim) }
            }
            return NSet(shape.dim, shape.stride, raw)
        }

        private fun <T, S> copycat(target: Any?, index: MutableList<Int>, element_maker: (IntArray) -> Any?): Any? {
            val sub = target as? NSet<S>
            return if (sub == null) {
                element_maker(index.toIntArray())
            } else {
                val start = index.size
                index.addAll(sub.dim.size, 0)
                val sub_raw = Array(sub.raw.size) {
                    copycat<T, S>(sub.raw[it], index, element_maker)
                            .apply { index.increment(sub.dim) }
                }
                index.subList(start, index.size).clear()
                NSet<T>(sub.dim, sub.stride, sub_raw)
            }
        }

        operator fun <T> invoke(vararg dim: Int, element_maker: (IntArray) -> Any? = { null }): NSet<T> {
            val stride = IntArray(dim.size)
            stride[stride.lastIndex] = 1
            for (a in stride.lastIndex - 1 downTo 0)
                stride[a] = dim[a + 1] * stride[a + 1]
            val total = dim[0] * stride[0]
            val index = mutableListOf(dim.size, 0)
            val raw = Array(total) {
                element_maker(index.toIntArray()).apply { index.increment(dim) }
            }
            return NSet(dim, stride, raw)
        }
    }

    private fun concat(indexable: Array<out Indexable>): IntArray {
        var total = 0
        indexable.forEach { total += it.idx.size }
        val idx = IntArray(total)
        var offset = 0
        indexable.forEach {
            System.arraycopy(it.idx, 0, idx, offset, it.idx.size)
            offset += it.idx.size
        }
        return idx
    }

    private fun <T> process(idx: IntArray, start: Int, set: Boolean, s: T?): T {
        var offset = 0
        val idx_size = idx.size - start
        if (idx_size < dim.size) throw RuntimeException("index.length=${idx.size - start}  < dim.length=${dim.size}")
        for (a in 0 until dim.size) {
            if (idx[start + a] < 0 || idx[start + a] > dim[a])
                throw ArrayIndexOutOfBoundsException("index[$a]= ${idx[start + a]} while dim[$a]=${dim[a]}")
            offset += idx[start + a] * stride[a]
        }
        return if (idx_size == dim.size) {
            if (set) raw[offset] = s
            raw[offset] as T
        } else {
            val sub = raw[offset] as? NSet<T> ?: throw RuntimeException("index dimension is larger than this set's element's dimension")
            sub.process(idx, start + dim.size, set, s)
        }
    }

    private inline fun _get(idx: IntArray): E = process<E>(idx, 0, false, null)

    private inline fun _set(idx: IntArray, s: E) = process(idx, 0, true, s)

    override operator fun get(vararg index: Int) = _get(index)

    override operator fun get(indexable: Indexable) = _get(indexable.idx)

    override operator fun get(vararg indexable: Indexable) = _get(concat(indexable))

    override operator fun set(vararg index: Int, s: E) {
        _set(index, s)
    }

    override operator fun set(indexable: Indexable, s: E) {
        _set(indexable.idx, s)
    }

    override operator fun set(vararg indexable: Indexable, s: E) {
        _set(concat(indexable), s)
    }

    operator fun set(vararg index: Int, s: NSet<E>) {
        require(index.size == dim.size) { "setting subset requires index.size == dim.size" }
        process(index, 0, true, s)
    }

    override fun iterator(): Iterator<E> {
        return object : Iterator<E> {
            var a = 0
            var inspect = false
            var sub: NSet<E>? = null
            var iter: Iterator<E>? = null

            override fun hasNext(): Boolean {
                while (a < raw.size) {
                    inspect_a()
                    if (sub == null || iter!!.hasNext()) return true
                    increment()
                }
                return false
            }

            override fun next(): E {
                while (a < raw.size) {
                    inspect_a()
                    if (sub != null) {
                        if (iter!!.hasNext())
                            return iter!!.next()
                        else
                            increment()
                    } else
                        raw[increment()] as E
                }
                throw NoSuchElementException()
            }

            fun increment(): Int {
                inspect = false
                return a++
            }

            private inline fun inspect_a() {
                if (!inspect) {
                    sub = raw[a] as? NSet<E>
                    iter = sub?.iterator()
                    inspect = true
                }
            }
        }
    }

    private inner class SubIterator(val index: MutableList<Int>) : Iterator<IntArray> {
        var a = 0
        var inspect = false
        var nset: NSet<E>? = null
        var iter: SubIterator? = null
        var clean: Boolean = false

        init {
            index.addAll(dim.size, 0)
        }

        override fun hasNext(): Boolean {
            while (a < raw.size) {
                inspect_a()
                if (nset == null || iter!!.hasNext()) return true
                increment()
            }
            if (!clean) {
                clean = true
                val start = index.size - dim.size
                index.subList(start, start + dim.size).clear()
            }
            return false
        }

        fun _next(): Boolean {
            while (a < raw.size) {
                inspect_a()
                when {
                    nset == null -> {
                        weak_increment()
                        return true
                    }
                    iter!!.hasNext() -> return iter!!._next()
                    else -> increment()
                }
            }
            return false
        }

        fun increment() {
            inspect = false
            index.increment(dim)
            a++
        }

        fun weak_increment() {
            inspect = false
            a++
        }

        inline fun inspect_a() {
            if (!inspect) {
                nset = raw[a] as? NSet<E>
                iter = nset?.indiceIterator(index)
                inspect = true
            }
        }

        inline fun isSub() = nset != null

        override fun next(): IntArray {
            if (!_next()) throw NoSuchElementException()
            val idx = index.toIntArray()
            if (isSub())
                index.increment(dim)
            return idx
        }
    }

    private fun indiceIterator(index: MutableList<Int>) = SubIterator(index)

    fun indices(): Iterator<IntArray> = indiceIterator(mutableListOf())
}

fun NSetMDP(gamma: Double, states: NSet<State?>, action_dim: (IntArray) -> IntArray) = MDP(
        states = states,
        gamma = gamma,
        v_maker = { NSet(states) { 0.0 } },
        q_maker = { NSet(states) { NSet<Double>(*action_dim(it)) { 0.0 } } },
        pi_maker = { NSet(states) })

fun NSetMDP(gamma: Double, state_dim: IntArray, action_dim: IntArray) = MDP(
        states = NSet(*state_dim) { State(it) },
        gamma = gamma,
        v_maker = { NSet(*state_dim) { 0.0 } },
        q_maker = { NSet(*state_dim, *action_dim) { 0.0 } },
        pi_maker = { NSet(*state_dim) })

fun NSetMDP(gamma: Double, state_dim: IntArray, action_dim: (IntArray) -> IntArray) = MDP(
        states = NSet(*state_dim) { State(it) },
        gamma = gamma,
        v_maker = { NSet(*state_dim) { 0.0 } },
        q_maker = { NSet(*state_dim) { NSet<Double>(*action_dim(it)) { 0.0 } } },
        pi_maker = { NSet(*state_dim) })