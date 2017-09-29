@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package lab.mars.rl.util

import lab.mars.rl.util.Bufkt.*
import lab.mars.rl.util.RandomAccessCollection.tuple2
import java.util.*

/**
 * <p>
 * Created on 2017-09-18.
 * </p>
 *
 * @author wumo
 */

class CompactNSet<E : Any>
constructor(internal val data: MutableBuf<Any>)
    : RandomAccessCollection<E> {

    class SubTree(val size: Int, val offset2nd: Int)

    val SubTree.lastIndex: Int
        get() = size - 1

    /**
     * 不存在[subtrees]为空的情况，如果[subtrees]为空，则必须将此
     * [Cell]替换为[value]
     * @param subtrees 此[Cell]中包含的子树集
     * @param value 此[Cell]存储的值
     */
    class Cell<E : Any>(val subtrees: MutableBuf<SubTree>, var value: E) {
        inline operator fun get(idx: Int): SubTree {
            if (idx < 0 || idx >= subtrees.size)
                throw IndexOutOfDimensionException()
            return subtrees[idx]
        }

        fun copy() = Cell(subtrees, value)
    }

    override fun <T : Any> copycat(element_maker: (IntBuf) -> T)
            : RandomAccessCollection<T> {
        val new_data = DefaultBuf.new<Any>(data.cap)
        for (a in 0..data.lastIndex)
            new_data += (data[a] as? Cell<E>)?.copy() ?: data[a]
        return CompactNSet<T>(new_data).apply {
            set { slot, _ ->
                element_maker(slot)
            }
        }
    }

    private inline fun <R : Any> operation(idx: Iterator<Int>, op: (Int) -> R): R {
        var offset = 0
        var level = 0
        while (true) {
            val d = idx.next()//子树索引
            val tmp = data[offset] as Cell<E>
            val subtree = tmp[level]
            require(d >= 0 && d < subtree.size)
            if (d == 0) level++
            else {
                level = 0
                offset = subtree.offset2nd + d - 1
            }
            if (!idx.hasNext()) break
        }
        return op(offset)
    }

    fun _get(idx: Int): E {
        val tmp = data[idx]
        return when (tmp) {
            is Cell<*> -> tmp.value
            else -> tmp
        } as E
    }

    fun _set(idx: Int, s: E) {
        val tmp = data[idx] as? Cell<E>
        if (tmp != null) tmp.value = s
        else data[idx] = s
    }

    override fun at(idx: Int): E {
        return _get(idx)
    }

    override fun get(idx: Index): E =
            operation(idx.iterator()) { _get(it) }

    override fun set(idx: Index, s: E) =
            operation(idx.iterator()) { _set(it, s) }

    override fun set(element_maker: (IntBuf, E) -> E) {
        dfs(0) { slot, offset ->
            _set(offset, element_maker(slot, _get(offset)))
        }
    }

    internal fun dfs(offset: Int, end: Int = 0, slot: MutableIntBuf = DefaultIntBuf.new(),
                     visit: (MutableIntBuf, Int) -> Unit) {
        val cell = data[offset] as? Cell<E> ?: return visit(slot, offset)
        val subtrees = cell.subtrees
        //防止遍历过程中改变，并且只会增多，不会减少
        val subtrees_size = subtrees.size
        slot.append(subtrees_size, 0)
        visit(slot, offset)//访问叶子结点
        for (level in subtrees_size - 1 downTo end) {
            slot.removeLast(1)
            val subtree = subtrees[level]
            if (subtree.size == 1) continue
            for (idx in 1 until subtree.size) {
                slot.append(idx)
                dfs(subtree.offset2nd + idx - 1, 0, slot, visit)
                slot.removeLast(1)
            }
        }
        slot.removeLast(end)
    }

    /**
     * 扩展[offset]位置上的leaf node为[size]branch node
     */
    internal fun expand(offset: Int, size: Int): SubTree {
        val tmp = data[offset] as? Cell<E>
                  ?: Cell(DefaultBuf.new(), data[offset] as E)
        val subtree = SubTree(size = size,
                              offset2nd = data.writePtr)
        tmp.subtrees += subtree
        data[offset] = tmp
        data.unfold(size - 1)
        return subtree
    }

    override fun indices() = Itr { slot, _ -> slot }

    override fun withIndices(): Iterator<tuple2<out IntBuf, E>> {
        var idxElement: tuple2<out IntBuf, E>? = null
        return Itr { slot, e ->
            idxElement?.apply { second = e }
            ?: tuple2(slot, e).apply { idxElement = this }
        }
    }

    inner class Itr<T>(
            private val visitor: (IntBuf, E) -> T
    ) : Iterator<T> {
        private var offset = 0
        private var visited = 0
        private val stack = LinkedList<tuple2<SubTree, Int>>()
        private val slot = DefaultIntBuf.new()

        override fun hasNext() = visited < data.size

        override fun next(): T {
            //correct the slot
            if (stack.isEmpty())
                deepDown()
            else while (true) {
                val toVisit = stack.peek()
                if (toVisit.second < toVisit.first.lastIndex) {
                    toVisit.second++
                    slot[slot.lastIndex]++
                    offset = toVisit.first.offset2nd + toVisit.second - 1
                    deepDown()
                    break
                } else {//finish visited this SubTree
                    stack.pop()
                    slot.removeLast(1)
                }
            }
            visited++
            val e = _get(offset)
            return visitor(slot, e)
        }

        private fun deepDown() {
            val cell = data[offset] as? Cell<E>
            if (cell != null) {
                val subtrees = cell.subtrees
                subtrees.forEach {
                    stack.push(tuple2(it, 0))
                }
                slot.append(subtrees.size, 0)
            }
        }
    }


    override fun iterator() = object : Iterator<E> {
        var a = 0
        override fun hasNext() = a < data.size

        override fun next() = _get(a++)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (withIndex in withIndices()) {
            sb.append(withIndex).append("\n")
        }
        return sb.toString()
    }
}