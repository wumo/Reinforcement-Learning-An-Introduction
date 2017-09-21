@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util

abstract class RandomAccessCollection<E : Any> : Iterable<E> {
    data class Pair<A, B>(var first: A, var second: B) {
        override fun toString(): String {
            return "$first=$second"
        }
    }

    /**
     * 构造一个与[shape]相同形状的[NSet]（维度、树深度都相同）
     */
    abstract fun <T : Any> copycat(element_maker: (IntBuf) -> T): RandomAccessCollection<T>

    abstract fun indices(): Iterator<IntBuf>

    abstract fun withIndices(): Iterator<Pair<out IntBuf, E>>

    abstract fun <T : Any> _get(idx: Index): T
    inline operator fun get(idx: Index): E = _get(idx)
    inline operator fun get(vararg idx: Int): E = get(DefaultIntBuf.reuse(idx))
    inline operator fun get(vararg indexable: Index): E = get(MultiIndex(indexable))

    abstract fun <T : Any> _set(idx: Index, s: T)
    inline operator fun set(idx: Index, s: E) = _set(idx, s)
    inline operator fun set(vararg idx: Int, s: E) = _set(DefaultIntBuf.reuse(idx), s)
    inline operator fun set(vararg indexable: Index, s: E) = _set(MultiIndex(indexable), s)
    /**
     * 对应位置元素为[RandomAccessCollection<E>]，则可以使用invoke操作符进行部分获取，
     * 由于获取的是子集，索引维度将要去掉前缀长度，如：原来通过`[0,0,0]`来索引，`invoke(0)`之后，则只能通过`[0,0]`来获取
     */
    inline operator fun invoke(idx: Index): RandomAccessCollection<E> = _get(idx)

    /**@see invoke */
    inline operator fun invoke(vararg idx: Int): RandomAccessCollection<E> = _get(DefaultIntBuf.reuse(idx))

    /**@see invoke */
    inline operator fun invoke(vararg indexable: Index): RandomAccessCollection<E> = _get(MultiIndex(indexable))

    inline operator fun set(idx: Index, s: RandomAccessCollection<E>) = _set(idx, s)
    inline operator fun set(vararg idx: Int, s: RandomAccessCollection<E>) = _set(DefaultIntBuf.reuse(idx), s)
    inline operator fun set(vararg indexable: Index, s: RandomAccessCollection<E>) = _set(MultiIndex(indexable), s)
    open fun<T:Any> set(element_maker: (IntBuf, E) -> T) {
        withIndices().forEach { (idx, value) -> _set(idx, element_maker(idx, value)) }
    }

    /**
     * 如果集合不为空，则执行[block]
     */
    open fun ifAny(block: RandomAccessCollection<E>.() -> Unit) {
        for (element in this) return block(this)
    }

    open fun isEmpty(): Boolean {
        for (element in this) return true
        return false
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (withIndex in withIndices())
            sb.append(withIndex).append("\n")
        return sb.toString()
    }
}