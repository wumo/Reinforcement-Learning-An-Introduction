@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

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

    operator fun get(dim: Index): E
    operator fun get(vararg dim: Int): E = get(DefaultIntBuf.reuse(dim))
    operator fun get(vararg dim: Index): E = get(MultiIndex(dim as Array<Index>))

    /**
     * 对应位置元素为[RandomAccessCollection<E>]，则可以使用invoke操作符进行部分获取，
     * 由于获取的是子集，索引维度将要去掉前缀长度，如：原来通过`[0,0,0]`来索引，`invoke(0)`之后，则只能通过`[0,0]`来获取
     */
    operator fun invoke(subset_dim: Index): RandomAccessCollection<E>

    /**@see invoke */
    operator fun invoke(vararg subset_dim: Int): RandomAccessCollection<E> = invoke(DefaultIntBuf.reuse(subset_dim))

    /**@see invoke */
    operator fun invoke(vararg subset_dim: Index): RandomAccessCollection<E> = invoke(MultiIndex(subset_dim as Array<Index>))

    /**
     * 返回第[idx]个元素，这里的元素顺序与[iterator()]的顺序一致
     */
    fun at(idx: Int): E {
        var i = 0
        for (element in this)
            if (i++ == idx) return element
        throw  NoSuchElementException()
    }

    operator fun set(dim: Index, s: E)
    operator fun set(vararg dim: Int, s: E) = set(DefaultIntBuf.reuse(dim), s)
    operator fun set(vararg dim: Index, s: E) = set(MultiIndex(dim as Array<Index>), s)

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
        for (element in this) return false
        return true
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
    operator fun set(subset_dim: Index, s: RandomAccessCollection<E>)
    operator fun set(vararg subset_dim: Int, s: RandomAccessCollection<E>) = set(DefaultIntBuf.reuse(subset_dim), s)
    operator fun set(vararg subset_dim: Index, s: RandomAccessCollection<E>) = set(MultiIndex(subset_dim as Array<Index>), s)

    fun <T : Any> raw_set(element_maker: (IntBuf, E) -> T) {
        withIndices().forEach { (idx, value) ->
            val tmp = element_maker(idx, value)
            (tmp as? RandomAccessCollection<E>)?.apply {
                set(idx, this)
            } ?: set(idx, tmp as E)
        }
    }
}