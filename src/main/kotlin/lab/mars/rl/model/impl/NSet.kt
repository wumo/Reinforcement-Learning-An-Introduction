package lab.mars.rl.model.impl

import lab.mars.rl.model.*

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
class NSet<E> private constructor(private val dim: IntArray, private val stride: IntArray, private val raw: Array<Any?>) :
        IndexedCollection<E> {
    override fun init(maker: (IntArray) -> Any?) {
        val index = IntArray(dim.size)
        for (i in 0 until raw.size)
            raw[i] = maker(index).apply {
                for (idx in index.size - 1 downTo 0) {
                    index[idx]++
                    if (index[idx] < dim[idx])
                        break
                    index[idx] = 0
                }
            }
    }

    companion object {
        operator fun <T> invoke(vararg dim: Int, element_maker: (IntArray) -> Any? = { null }): NSet<T> {
            val stride = IntArray(dim.size)
            stride[stride.size - 1] = 1
            for (a in stride.size - 2 downTo 0)
                stride[a] = dim[a + 1] * stride[a + 1]
            val total = dim[0] * stride[0]
            val index = IntArray(dim.size)
            val raw = Array(total) {
                element_maker(index).apply {
                    for (idx in index.size - 1 downTo 0) {
                        index[idx]++
                        if (index[idx] < dim[idx])
                            break
                        index[idx] = 0
                    }
                }
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
            val sub = raw[offset] as? NSet<T> ?: throw RuntimeException("idx.size>dim.size && raw[offset] !is NSet")
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
                    a++
                    invalidate_inspect()
                }
                return false
            }

            override fun next(): E {
                inspect_a()
                return if (sub != null)
                    iter!!.next()
                else {
                    invalidate_inspect()
                    raw[a++] as E
                }
            }

            private inline fun inspect_a() {
                if (!inspect) {
                    sub = raw[a] as? NSet<E>
                    iter = sub?.iterator()
                    inspect = true
                }
            }

            private inline fun invalidate_inspect() {
                inspect = false
            }
        }
    }
}

fun NSetMDP(gamma: Double, state_dim: IntArray, action_dim: IntArray) = MDP(
        states = NSet(*state_dim) { State(*it) },
        gamma = gamma,
        v_maker = { NSet(*state_dim) { 0.0 } },
        q_maker = { NSet(*state_dim, *action_dim) { 0.0 } },
        pi_maker = { NSet(*state_dim) })

fun NSetMDP(gamma: Double, state_dim: IntArray, action_dim: (IntArray) -> IntArray) = MDP(
        states = NSet(*state_dim) { State(*it) },
        gamma = gamma,
        v_maker = { NSet(*state_dim) { 0.0 } },
        q_maker = { NSet(*state_dim) { NSet<Double>(*action_dim(it)) } },
        pi_maker = { NSet(*state_dim) })