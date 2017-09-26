@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package lab.mars.rl.util

import lab.mars.rl.util.RandomAccessCollection.tuple2

/**
 * <p>
 * Created on 2017-09-18.
 * </p>
 *
 * @author wumo
 */
/**
 * @param value 包含的元素值
 * @param range 包含元素数
 * @param offset 子集的偏置
 */
class CompactNSet<E : Any> private constructor(
        private val data: Buf<Any>,
        private val range: Int,
        private val offset: Int) : RandomAccessCollection<E> {

    override fun <T : Any> copycat(element_maker: (IntBuf) -> T): RandomAccessCollection<T> {
        TODO("not implemented")
    }

    private inline fun <R : Any> operation(idx: Iterator<Int>, op: (Int) -> R): R {
        var offset = 0
        while (idx.hasNext()) {
            val d = idx.next()
            val tmp = data[offset]
            if (tmp !is CompactNSet<*> || d < 0 || d >= tmp.range)
                throw IndexOutOfDimensionException()
            offset = tmp.offset + d
        }
        return op(offset)
    }

    override fun get(idx: Index) =
            operation(idx.iterator()) { data[it] } as E

    override fun set(idx: Index, s: E) =
            operation(idx.iterator()) { data[it] = s }


    override fun indices(): Iterator<IntBuf> {
        TODO("not implemented")
    }

    override fun withIndices(): Iterator<tuple2<out IntBuf, E>> {
        TODO("not implemented")
    }


    override fun iterator() = object : Iterator<E> {
        var a = 0
        override fun hasNext(): Boolean {
            while (a < data.size) {
                if (data[a] !is CompactNSet<*>) return true
                a++
            }
            return false
        }

        override fun next() = data[a] as E
    }

}