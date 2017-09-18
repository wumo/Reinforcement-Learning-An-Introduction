@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.util

import com.sun.org.apache.bcel.internal.generic.RETURN
import java.util.*

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
 * 如数组链表为：0000123，`Dim=[3,3,4]`，则进位加1仅影响最后`Dim.size=3`个元素`123`，
 * `123++`的结果为`200`
 *
 * @receiver 表示index的数组链表
 */
fun DefaultIntSlice.increment(dim: IntArray) {
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
 *如: `val a=Nset(2 x 3)`定义了一个2x3的矩阵，可以使用`a[0,0]=0`这样的用法
 *
 * 2. 可以嵌套使用[NSet]，定义不规则形状的树形结构
 * 如: `val a=Nset(2), a[0]=Nset(1), a[1]=Nset(2)`定义了一个不规则的结构，
 * 第一个元素长度为1第二个元素长度为2；同样可以通过`[]`来进行取值赋值，
 * 如`a[0,0]=0, a[1, 1]`，但`a[0,1]`和`a[1,2]`就会导致[ArrayIndexOutOfBoundsException]
 *
 * @param dim 根节点的维度定义
 * @param stride 各维度在一维数组上的跨度
 * @param root 根节点一维数组
 */
class NSet<E> private constructor(private val dim: IntArray, private val stride: IntArray, private val root: Array<Any?>) :
        RandomAccessCollection<E>() {
    companion object {
        inline fun <T> of(vararg elements: T): NSet<T> {
            var i = 0
            return invoke(elements.size) { elements[i++] }
        }

        /**
         * 构造一个与[shape]相同形状的[NSet]（维度、树深度都相同）
         */
        operator fun <T> invoke(shape: NSet<*>, element_maker: (IntSlice) -> Any? = { null }): NSet<T> =
                NSet<T>(shape.dim, shape.stride, Array(shape.root.size) { null }).apply {
                    val index = DefaultIntSlice.zero(shape.dim.size)
                    for (idx in 0 until this.root.size)
                        copycat(this, shape.root[idx], index, element_maker)
                                .apply { index.increment(shape.dim) }
                }

        operator fun <T> invoke(dimension: Any, element_maker: (IntSlice) -> Any? = { null }): NSet<T> {
            val _dim = dimension.toDim()
            return make(_dim.rootDim, _dim.sub, DefaultIntSlice.new(), element_maker)
        }

        private fun <T> copycat(origin: NSet<T>, prototype: Any?, index: DefaultIntSlice, element_maker: (IntSlice) -> Any?): Any? =
                when (prototype) {
                    is NSet<*> -> NSet<T>(prototype.dim, prototype.stride, Array(prototype.root.size) { null }).apply {
                        parent = origin
                        index.append(prototype.dim.size, 0)
                        for (idx in 0 until root.size)
                            copycat(this, prototype.root[idx], index, element_maker)
                                    .apply { index.increment(prototype.dim) }
                        index.removeLast(prototype.dim.size)
                    }
                    else -> origin.refParent(element_maker(index))
                }

        private val zeroDim = DefaultIntSlice.of(0)
        private fun <T> make(rootDim: IntSlice, sub: Array<Dimension>, index: DefaultIntSlice,
                             element_maker: (IntSlice) -> Any? = { null }): NSet<T> {
            val dim = rootDim.toIntArray()
            val isZero = dim.size == 1 && dim[0] == 0
            if (isZero) {
                dim[0] = sub.size
                index.append(0)
            } else
                index.append(rootDim.size, 0)

            val stride = IntArray(dim.size)
            stride[stride.lastIndex] = 1
            for (a in stride.lastIndex - 1 downTo 0)
                stride[a] = dim[a + 1] * stride[a + 1]

            val total = dim[0] * stride[0]
            return NSet(dim, stride, Array(total) {
                when {
                    sub.isEmpty() -> element_maker(index)
                    isZero -> make<T>(sub[it].rootDim, sub[it].sub, index, element_maker)
                    else -> make<T>(zeroDim, sub, index, element_maker)
                }.apply { index.increment(dim) }
            }.apply { index.removeLast(dim.size) })
        }
    }

    private var parent: NSet<E>? = null
    private fun refParent(s: Any?): Any? {
        val sub = s as? NSet<E>
        if (sub != null) sub.parent = this
        return s
    }

    private fun unrefParent(s: Any?): Any? {
        val sub = s as? NSet<E>
        if (sub != null) sub.parent = null
        return s
    }

    private fun <T> get_or_set(idx: Index, start: Int, set: Boolean, s: T?): T {
        var offset = 0
        val idx_size = idx.size - start
        if (idx_size < dim.size) throw RuntimeException("index.length=${idx.size - start}  < Dim.length=${dim.size}")
        idx.forEach(start, start + dim.lastIndex) { idx, value ->
            val a = idx - start
            if (value < 0 || value > dim[a])
                throw ArrayIndexOutOfBoundsException("index[$a]= $value while Dim[$a]=${dim[a]}")
            offset += value * stride[a]
        }

        return if (idx_size == dim.size) {
            val tmp = root[offset]
            if (set) {
                if (tmp != null) unrefParent(tmp)
                root[offset] = refParent(s)
            }
            tmp as T
        } else {
            val sub = root[offset] as? NSet<T> ?: throw RuntimeException("index dimension is larger than this set's element's dimension")
            sub.get_or_set(idx, start + dim.size, set, s)
        }
    }

    override fun get(idx: Index): E = get_or_set<E>(idx, 0, false, null)

    override fun set(idx: Index, s: E) {
        get_or_set(idx, 0, true, s)
    }

    operator fun set(vararg index: Int, s: NSet<E>) {
        require(index.size == dim.size) { "setting subset requires index.size == Dim.size" }
        get_or_set(DefaultIntSlice.reuse(index), 0, true, s)
    }

    override fun iterator() = GeneralIterator<E>().apply { traverse = Traverse(this, {}, {}, {}, { it }) }

    fun indices() = GeneralIterator<IntSlice>().apply {
        val index = DefaultIntSlice.zero(this.set.dim.size).apply { this[lastIndex] = -1 }
        traverse = Traverse(this,
                            forward = {
                                index.apply {
                                    append(current.set.dim.size, 0)
                                    this[lastIndex] = -1
                                }
                            },
                            backward = { index.removeLast(current.set.dim.size) },
                            translate = { index.increment(current.set.dim) },
                            visitor = { index })
    }

    data class Pair<A, B>(var first: A, var second: B) {
        override fun toString(): String {
            return "($first, $second)"
        }
    }

    fun withIndices() = GeneralIterator<Pair<out IntSlice, E>>().apply {
        val index = DefaultIntSlice.zero(this.set.dim.size).apply { this[lastIndex] = -1 }
        var pair: Pair<out IntSlice, E>? = null
        traverse = Traverse(this,
                            forward = {
                                index.apply {
                                    append(current.set.dim.size, 0)
                                    this[lastIndex] = -1
                                }
                            },
                            backward = { index.removeLast(current.set.dim.size) },
                            translate = { index.increment(current.set.dim) },
                            visitor = {
                                val tmp = pair ?: lab.mars.rl.util.NSet.Pair(index, it)
                                pair = tmp
                                tmp.second = it
                                tmp
                            })
    }

    inner class GeneralIterator<out T> : Iterator<T> {
        /**
         * @param current 当前正待着的节点
         * @param forward 移到了未探索过的更深的节点
         * @param backward 正要从当前节点退回去到[parent]
         * @param translate 当前节点进行宽度范围内的下一个搜索
         * @param visitor 获取当前非子树元素的值
         */
        inner class Traverse(var current: GeneralIterator<T>,
                             val forward: Traverse.() -> Unit,
                             val backward: Traverse.() -> Unit,
                             val translate: Traverse.() -> Unit,
                             val visitor: Traverse.(E) -> T)

        internal lateinit var traverse: Traverse
        private var visited = -1
        internal val set = this@NSet
        private var parent: GeneralIterator<T> = this

        override fun hasNext(): Boolean {
            return dfs({ true }, { false })
        }

        override fun next(): T {
            return dfs({ traverse.current.increment();traverse.visitor(traverse, it) }, { throw NoSuchElementException() })
        }

        private inline fun increment(): Int {
            traverse.translate(traverse)
            return visited++
        }

        private inline fun <T> dfs(element: (E) -> T, nomore: () -> T): T {
            while (true) {
                while (traverse.current.visited + 1 < traverse.current.set.root.size) {
                    val tmp = traverse.current
                    val next = tmp.set.root[tmp.visited + 1]
                    tmp.inspect_next_type(next) ?: return element(next as E)
                }
                if (traverse.current === this) return nomore()
                traverse.backward(traverse)
                traverse.current = traverse.current.parent
            }
        }

        private inline fun inspect_next_type(next: Any?): NSet<E>? {
            val next_type = next as? NSet<E>
            next_type?.apply {
                val new = this.GeneralIterator<T>()
                new.traverse = traverse
                new.parent = traverse.current
                new.parent.increment()
                traverse.current = new
                traverse.forward(traverse)
            }
            return next_type
        }
    }
}