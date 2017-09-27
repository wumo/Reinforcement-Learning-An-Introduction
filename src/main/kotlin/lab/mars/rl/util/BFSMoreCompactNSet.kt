@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package lab.mars.rl.util

import lab.mars.rl.util.RandomAccessCollection.tuple2
import java.util.*

/**
 * <p>
 * Created on 2017-09-18.
 * </p>
 *
 * @author wumo
 */

class BFSMoreCompactNSet<E : Any>
constructor(internal val data: Buf<Any>)
    : RandomAccessCollection<E> {

    class SubTree(val size: Int, val offset2nd: Int)

    val SubTree.lastOffset: Int
        get() = offset2nd + size - 2
    val SubTree.lastIndex: Int
        get() = size - 1

    /**
     * 不存在[subtrees]为空的情况，如果[subtrees]为空，则必须将此
     * [Cell]替换为[value]
     * @param subtrees 此[Cell]中包含的子树集
     * @param value 此[Cell]存储的值
     */
    class Cell<E : Any>(val subtrees: Buf<SubTree>, var value: E) {
        inline operator fun get(idx: Int): SubTree {
            if (idx < 0 || idx >= subtrees.size)
                throw IndexOutOfDimensionException()
            return subtrees[idx]
        }
    }

    override fun <T : Any>
            copycat(element_maker: (IntBuf) -> T)
            : RandomAccessCollection<T> {
        TODO("not implemented")
    }

    private inline fun <R : Any>
            operation(idx: Iterator<Int>, op: (Int) -> R): R {
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

    override fun get(idx: Index): E =
            operation(idx.iterator()) { _get(it) }

    override fun set(idx: Index, s: E) =
            operation(idx.iterator()) { _set(it, s) }

    override fun set(element_maker: (IntBuf, E) -> E) {

    }

    fun dfs(offset: Int, end: Int = 0, slot: MutableIntBuf = DefaultIntBuf.new(),
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

    override fun indices(): Iterator<IntBuf> {
        TODO("not implemented")
    }

    override fun withIndices() = object : Iterator<tuple2<out IntBuf, E>> {
        var offset = 0
        var visited = 0
        val stack = LinkedList<tuple2<SubTree, Int>>()
        val slot = DefaultIntBuf.new()
        var idxElement: tuple2<out IntBuf, E>? = null

        override fun hasNext() = visited < data.size

        override fun next(): tuple2<out IntBuf, E> {
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
            return idxElement?.apply { second = e }
                   ?: tuple2<IntBuf, E>(slot, e).apply { idxElement = this }
        }

        fun deepDown() {
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