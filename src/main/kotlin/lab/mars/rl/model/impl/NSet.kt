@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model.impl

import lab.mars.rl.model.*
import lab.mars.rl.util.IntSlice
import java.util.NoSuchElementException

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */

/**
 * 将数组链表的最后dim.size个元素构成的数字依据dim进行进位加1
 *
 * 如数组链表为：0000123，`dim=[3,3,4]`，则进位加1仅影响最后`dim.size=3`个元素`123`，
 * `123++`的结果为`200`
 *
 * @receiver 表示index的数组链表
 */
fun IntSlice.increment(dim: IntArray) {
    val offset = lastIndex - dim.size + 1
    for (idx in dim.lastIndex downTo 0) {
        val this_idx = offset + idx
        this[this_idx]++
        if (this[this_idx] < dim[idx])
            break
        this[this_idx] = 0
    }
}

/**
 * 1. 可以定义任意维的多维数组，并使用`[]`进行取值赋值
 *如: `val a=Nset(2,3)`定义了一个2x3的矩阵，可以使用`a[0,0]=0`这样的用法
 *
 * 2. 可以嵌套使用[NSet]，定义不规则形状的树形结构
 * 如: `val a=Nset(2), a[0]=Nset(1), a[1]=Nset(2)`定义了一个不规则的结构，
 * 第一个元素长度为1第二个元素长度为2；同样可以通过`[]`来进行取值赋值，
 * 如`a[0,0]=0, a[1, 1]`，但`a[0,1]`和`a[1,2]`就会导致[ArrayIndexOutOfBoundsException]
 *
 */
class NSet<E> private constructor(private val dim: IntArray, private val stride: IntArray, private val root: Array<Any?>) :
        IndexedCollection<E> {
    /**
     * 使用构造lambda [maker]来为数组的每个元素初始化
     * @param maker 提供了每个元素的index
     */
    override fun init(maker: (IntArray) -> Any?) {
        val index = IntSlice(dim.size)
        for (i in 0 until root.size)
            root[i] = maker(index.toIntArray()).apply {
                index.increment(dim)
            }
    }

    companion object {
        operator fun <T> invoke(vararg dim: Int, element_maker: (IntArray) -> Any? = { null }): NSet<T> {
            val stride = IntArray(dim.size)
            stride[stride.lastIndex] = 1
            for (a in stride.lastIndex - 1 downTo 0)
                stride[a] = dim[a + 1] * stride[a + 1]
            val total = dim[0] * stride[0]
            val index = IntSlice(dim.size)
            val raw = Array(total) {
                element_maker(index.toIntArray()).apply { index.increment(dim) }
            }
            return NSet(dim, stride, raw)
        }

        /**
         * 构造一个与[shape]相同形状的[NSet]（维度、树深度都相同）
         */
        operator fun <T> invoke(shape: NSet<*>, element_maker: (IntArray) -> Any? = { null }): NSet<T> {
            val index = IntSlice(shape.dim.size)
            val root = Array(shape.root.size) {
                copycat<T>(shape.root[it], index, element_maker)
                        .apply { index.increment(shape.dim) }
            }
            return NSet(shape.dim, shape.stride, root)
        }

        private fun <T> copycat(prototype: Any?, index: IntSlice, element_maker: (IntArray) -> Any?): Any? {
            return when (prototype) {
                is NSet<*> -> {
                    val start = index.size
                    index.append(prototype.dim.size, 0)
                    val sub_raw = Array(prototype.root.size) {
                        copycat<T>(prototype.root[it], index, element_maker)
                                .apply { index.increment(prototype.dim) }
                    }
                    index.remove(start, index.size)
                    NSet<T>(prototype.dim, prototype.stride, sub_raw)
                }
                else -> element_maker(index.toIntArray())
            }
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
            if (set) root[offset] = s
            root[offset] as T
        } else {
            val sub = root[offset] as? NSet<T> ?: throw RuntimeException("index dimension is larger than this set's element's dimension")
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

    override fun iterator() = ElementIterator()

    fun indices(): Iterator<IntArray> = indiceIterator(IntSlice())
    private fun indiceIterator(index: IntSlice) = IndexIterator(index)

    inner class ElementIterator : Iterator<E> {
        private var a = 0
        private var inspect = false
        private var sub: NSet<E>? = null
        private var iter: Iterator<E>? = null

        override fun hasNext(): Boolean {
            while (a < root.size) {
                inspect_a()
                if (sub == null || iter!!.hasNext()) return true
                increment()
            }
            return false
        }

        override fun next(): E {
            while (a < root.size) {
                inspect_a()
                if (sub != null) {
                    if (iter!!.hasNext())
                        return iter!!.next()
                    else
                        increment()
                } else
                    root[increment()] as E
            }
            throw NoSuchElementException()
        }

        private inline fun increment(): Int {
            inspect = false
            return a++
        }

        private inline fun inspect_a() {
            if (!inspect) {
                sub = root[a] as? NSet<E>
                iter = sub?.iterator()
                inspect = true
            }
        }
    }

    private inner class IndexIterator(val index: IntSlice) : Iterator<IntArray> {
        var a = 0
        var inspect = false
        var nset: NSet<E>? = null
        var iter: IndexIterator? = null
        var clean: Boolean = false

        init {
            index.append(dim.size, 0)
        }

        override fun hasNext(): Boolean {
            while (a < root.size) {
                inspect_a()
                if (nset == null || iter!!.hasNext()) return true
                increment()
            }
            if (!clean) {
                clean = true
                val start = index.size - dim.size
                index.remove(start, start + dim.size)
            }
            return false
        }

        fun _next(): Boolean {
            while (a < root.size) {
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
                nset = root[a] as? NSet<E>
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