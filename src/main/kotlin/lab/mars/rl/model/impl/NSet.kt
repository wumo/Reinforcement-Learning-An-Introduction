package lab.mars.rl.model.impl

import lab.mars.rl.model.Indexable
import lab.mars.rl.model.IndexedCollection
import lab.mars.rl.model.MDP
import lab.mars.rl.model.State

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
class NSet<E>(private val dim: IntArray, private val stride: IntArray, val raw: Array<Any?>) :
        IndexedCollection<E> {
    override fun init(maker: (IntArray) -> Any?) {
        val index = IntArray(dim.size)
        for (i in 0 until raw.size) {
            raw[i] = maker(index).apply {
                for (idx in index.size - 1 downTo 0) {
                    index[idx]++
                    if (index[idx] < dim[idx])
                        break
                    index[idx] = 0
                }
            }
        }
    }

    companion object {
        operator inline fun <reified T> invoke(vararg dim: Int): NSet<T>
                = invoke(*dim) { null as T }

        operator inline fun <reified T> invoke(vararg dim: Int, element_maker: (IntArray) -> Any?): NSet<T> {
            val stride = IntArray(dim.size)
            stride[stride.size - 1] = 1
            for (a in stride.size - 2 downTo 0)
                stride[a] = dim[a + 1] * stride[a + 1]
            val total = dim[0] * stride[0]
            val index = IntArray(dim.size)
            val raw = Array(total, {
                element_maker(index).apply {
                    for (idx in index.size - 1 downTo 0) {
                        index[idx]++
                        if (index[idx] < dim[idx])
                            break
                        index[idx] = 0
                    }
                }
            })
            return NSet(dim, stride, raw)
        }
    }

    private fun combine(indexable: Array<out Indexable>): IntArray {
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

    private fun _set(idx: IntArray, start: Int, s: E) {
        var offset = 0
        val idx_size = idx.size - start;
        if (idx_size >= dim.size) {
            for (a in 0 until dim.size) {
                if (idx[start + a] < 0 || idx[start + a] > dim[a])
                    throw ArrayIndexOutOfBoundsException("index[$a]= ${idx[start + a]} while dim[$a]=${dim[a]}")
                offset += idx[start + a] * stride[a]
            }
            if (idx_size > dim.size) {
                val sub = raw[offset] as? NSet<E> ?: throw RuntimeException("idx.size>dim.size && raw[offset] !is NSet")
                return sub._set(idx, start + dim.size, s)
            }
            raw[offset] = s
        } else
            throw RuntimeException("index.length=${idx.size - start}  < dim.length=${dim.size}")
    }

    private fun _get(idx: IntArray, start: Int): E {
        var offset = 0
        val idx_size = idx.size - start;
        if (idx_size >= dim.size) {
            for (a in 0 until dim.size) {
                if (idx[start + a] < 0 || idx[start + a] > dim[a])
                    throw ArrayIndexOutOfBoundsException("index[$a]= ${idx[start + a]} while dim[$a]=${dim[a]}")
                offset += idx[start + a] * stride[a]
            }
            if (idx_size > dim.size) {
                if (raw[offset] is NSet<*>) {
                    val sub = raw[offset] as NSet<E>
                    return sub._get(idx, start + dim.size)
                } else
                    throw RuntimeException("idx.size>dim.size && raw[offset] !is NSet")
            }
            return raw[offset] as E
        } else
            throw RuntimeException("index.length=${idx.size - start}  < dim.length=${dim.size}")
    }

    override operator fun get(vararg index: Int) = _get(index, 0)

    override operator fun get(indexable: Indexable) = _get(indexable.idx, 0)

    override operator fun get(vararg indexable: Indexable) = _get(combine(indexable), 0)

    override operator fun set(vararg index: Int, s: E) {
        _set(index, 0, s)
    }

    override operator fun set(indexable: Indexable, s: E) {
        _set(indexable.idx, 0, s)
    }

    override operator fun set(vararg indexable: Indexable, s: E) {
        _set(combine(indexable), 0, s)
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

fun NSetMDP(state_dim: IntArray, action_dim: IntArray, gamma: Double) = MDP(
        states = NSet(*state_dim) { State(*it) },
        gamma = gamma,
        v_maker = { NSet(*state_dim) { 0.0 } },
        q_maker = { NSet(*state_dim, *action_dim) { 0.0 } },
        pi_maker = { NSet(*state_dim) })

