@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "OVERRIDE_BY_INLINE")

package lab.mars.rl.util.collection

import lab.mars.rl.util.buf.*
import lab.mars.rl.util.exception.IndexOutOfDimensionException
import lab.mars.rl.util.tuples.tuple2
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
fun MutableIntBuf.increment(dim: IntArray) {
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
 * 直接使用[elements]构建[NSet]的全部内容
 */
inline fun <T : Any> nsetOf(vararg elements: T) = NSet<T>(intArrayOf(elements.size), intArrayOf(1), Array(elements.size) { elements[it] })

val emptyNSet = NSet<Any>(IntArray(0), IntArray(0), Array(0) {})
inline fun <E : Any> emptyNSet(): NSet<E> = emptyNSet as NSet<E>

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
class NSet<E : Any>(private val dim: IntArray, private val stride: IntArray, private val root: Array<Any>) :
        ExtendableRAC<E> {
    override fun <T : Any> copycat(element_maker: (Index) -> T): IndexedCollection<T> {
        val index = DefaultIntBuf.zero(dim.size)
        return NSet(dim, stride, Array(root.size) {
            copycat(root[it], index, element_maker)
                    .apply { index.increment(dim) }
        })
    }

    private fun <T : Any> copycat(prototype: Any, index: DefaultIntBuf, element_maker: (IntBuf) -> T): Any =
            when (prototype) {
                is NSet<*> -> {
                    index.append(prototype.dim.size, 0)
                    NSet<T>(prototype.dim, prototype.stride, Array(prototype.root.size) {
                        copycat(prototype.root[it], index, element_maker)
                                .apply { index.increment(prototype.dim) }
                    }).apply { index.removeLast(prototype.dim.size) }
                }
                else -> element_maker(index)
            }

    override fun <T : Any> raw_set(element_maker: (Index, E) -> T) {
        val index = DefaultIntBuf.new()
        reset(this, index, element_maker)
    }

    private fun <T : Any> reset(sub: NSet<E>, index: DefaultIntBuf, element_maker: (IntBuf, E) -> T) {
        index.append(sub.dim.size, 0)
        for (a in 0 until sub.root.size) {
            val tmp = sub.root[a] as? NSet<E>
            if (tmp == null)
                sub.root[a] = element_maker(index, sub.root[a] as E)
            else
                reset(tmp, index, element_maker)
            index.increment(sub.dim)
        }
        index.removeLast(sub.dim.size)
    }

    private fun <T : Any> get_or_set(idxIter: Iterator<Int>, op: (Any) -> Any): T {
        var offset = 0
        for (a in 0 until dim.size) {
            if (!idxIter.hasNext())
                throw IndexOutOfDimensionException()
            val value = idxIter.next()
            if (value < 0 || value > dim[a])
                throw IndexOutOfDimensionException()
            offset += value * stride[a]
        }

        return if (!idxIter.hasNext()) {
            root[offset] = op(root[offset])
            root[offset] as T
        } else {
            val sub = root[offset] as? NSet<T> ?: throw IndexOutOfDimensionException()
            sub.get_or_set(idxIter, op)
        }
    }

    override fun ifAny(block: IndexedCollection<E>.(IndexedCollection<E>) -> Unit) {
        if (!root.isEmpty()) block(this, this)
    }

    override fun isEmpty() = root.isEmpty()

    override fun get(dim: Index): E = _get(dim)

    override fun set(dim: Index, s: E) {
        _set(dim, s)
    }

    override fun set(subset_dim: Index, s: IndexedCollection<E>) {
        _set(subset_dim, s)
    }

    override fun invoke(subset_dim: Index): IndexedCollection<E> = _get(subset_dim)

    private fun <T : Any> _get(idx: Index): T = get_or_set(idx.iterator()) { it }

    private fun <T : Any> _set(idx: Index, s: T) {
        get_or_set<T>(idx.iterator()) { s }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for ((idx, value) in withIndices())
            sb.append("$idx=$value").append("\n")
        return sb.toString()
    }

    override fun iterator() = GeneralIterator<E>().apply { traverser = Traverser(this, {}, {}, {}, { it }) }

    override fun indices() = GeneralIterator<Index>().apply {
        val index = DefaultIntBuf.zero(this.set.dim.size).apply { this[lastIndex] = -1 }
        traverser = Traverser(this,
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

    override fun withIndices(): Iterator<tuple2<out Index, E>> = GeneralIterator<tuple2<out IntBuf, E>>().apply {
        val index = DefaultIntBuf.zero(this.set.dim.size).apply { this[lastIndex] = -1 }
        var tuple2: tuple2<out IntBuf, E>? = null
        traverser = Traverser(this,
                              forward = {
                                  index.apply {
                                      append(current.set.dim.size, 0)
                                      this[lastIndex] = -1
                                  }
                              },
                              backward = { index.removeLast(current.set.dim.size) },
                              translate = { index.increment(current.set.dim) },
                              visitor = {
                                  val tmp = tuple2 ?: tuple2(index, it)
                                  tuple2 = tmp
                                  tmp._2 = it
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
        inner class Traverser(var current: GeneralIterator<T>,
                              val forward: Traverser.() -> Unit,
                              val backward: Traverser.() -> Unit,
                              val translate: Traverser.() -> Unit,
                              val visitor: Traverser.(E) -> T)

        internal lateinit var traverser: Traverser
        private var visited = -1
        internal val set = this@NSet
        private var parent: GeneralIterator<T> = this

        override fun hasNext() = dfs({ true }, { false })

        override fun next() =
                dfs({ traverser.current.increment();traverser.visitor(traverser, it) }, { throw NoSuchElementException() })

        private inline fun increment(): Int {
            traverser.translate(traverser)
            return visited++
        }

        private inline fun <T> dfs(element: (E) -> T, nomore: () -> T): T {
            while (true) {
                while (traverser.current.visited + 1 < traverser.current.set.root.size) {
                    val tmp = traverser.current
                    val next = tmp.set.root[tmp.visited + 1]
                    tmp.inspect_next_type(next) ?: return element(next as E)
                }
                if (traverser.current === this) return nomore()
                traverser.backward(traverser)
                traverser.current = traverser.current.parent
            }
        }

        private inline fun inspect_next_type(next: Any?): NSet<E>? {
            val next_type = next as? NSet<E>
            next_type?.apply {
                val new = this.GeneralIterator<T>()
                new.traverser = traverser
                new.parent = traverser.current
                new.parent.increment()
                traverser.current = new
                traverser.forward(traverser)
            }
            return next_type
        }
    }
}