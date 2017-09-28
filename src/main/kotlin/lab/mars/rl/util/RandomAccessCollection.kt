@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util

import lab.mars.rl.util.Bufkt.DefaultIntBuf
import lab.mars.rl.util.Bufkt.IntBuf

interface RandomAccessCollection<E : Any> : Iterable<E> {
    data class tuple2<A, B>(var first: A, var second: B) {
        override fun toString(): String {
            return "$first=$second"
        }
    }

    /**
     * 构造一个与此集合相同形状的[RandomAccessCollection]（维度、树深度都相同）
     */
    fun <T : Any> copycat(element_maker: (IntBuf) -> T): RandomAccessCollection<T>

    fun indices(): Iterator<IntBuf>

    fun withIndices(): Iterator<tuple2<out IntBuf, E>>

    operator fun get(idx: Index): E
    operator fun get(vararg idx: Int): E = get(DefaultIntBuf.reuse(idx))
    operator fun get(vararg indexable: Index): E = get(MultiIndex(indexable))

    /**
     * 返回第[idx]个元素，这里的元素顺序与[iterator()]的顺序一致
     */
    fun at(idx: Int): E {
        var i = 0
        for (element in this)
            if (i++ == idx) return element
        throw  NoSuchElementException()
    }

    operator fun set(idx: Index, s: E)
    operator fun set(vararg idx: Int, s: E) = set(DefaultIntBuf.reuse(idx), s)
    operator fun set(vararg indexable: Index, s: E) = set(MultiIndex(indexable), s)

    fun set(element_maker: (IntBuf, E) -> E) {
        withIndices().forEach { (idx, value) -> set(idx, element_maker(idx, value)) }
    }

    /**
     * 如果集合不为空，则执行[block]
     */
    fun ifAny(block: RandomAccessCollection<E>.() -> Unit) {
        for (element in this) return block(this)
    }

    fun isEmpty(): Boolean {
        for (element in this) return true
        return false
    }

    val size: Int
        get() {
            var count = 0
            for (element in this)
                count++
            return count
        }

//    override fun toString(): String {
//        val sb = StringBuilder()
//        for (withIndex in withIndices())
//            sb.append(withIndex).append("\n")
//        return sb.toString()
//    }
}

interface ExtendableRAC<E : Any> : RandomAccessCollection<E> {
    operator fun set(idx: Index, s: RandomAccessCollection<E>)
    operator fun set(vararg idx: Int, s: RandomAccessCollection<E>) = set(DefaultIntBuf.reuse(idx), s)
    operator fun set(vararg indexable: Index, s: RandomAccessCollection<E>) = set(MultiIndex(indexable), s)
    /**
     * 对应位置元素为[RandomAccessCollection<E>]，则可以使用invoke操作符进行部分获取，
     * 由于获取的是子集，索引维度将要去掉前缀长度，如：原来通过`[0,0,0]`来索引，`invoke(0)`之后，则只能通过`[0,0]`来获取
     */
    operator fun invoke(idx: Index): RandomAccessCollection<E>

    /**@see invoke */
    operator fun invoke(vararg idx: Int): RandomAccessCollection<E> = invoke(DefaultIntBuf.reuse(idx))

    /**@see invoke */
    operator fun invoke(vararg indexable: Index): RandomAccessCollection<E> = invoke(MultiIndex(indexable))

    fun <T : Any> raw_set(element_maker: (IntBuf, E) -> T) {
        withIndices().forEach { (idx, value) ->
            val tmp = element_maker(idx, value)
            (tmp as? RandomAccessCollection<E>)?.apply {
                set(idx, this)
            } ?: set(idx, tmp as E)
        }
    }
}