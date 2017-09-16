@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util

abstract class RandomAccessCollection<E> : Iterable<E> {
    abstract operator fun get(idx: Index): E
    inline operator fun get(vararg idx: Int): E = get(DefaultIntSlice.reuse(idx))
    inline operator fun get(vararg indexable: Index): E = get(WrappedIndex.of(indexable))

    abstract operator fun set(idx: Index, s: E)

    inline operator fun set(vararg idx: Int, s: E) {
        set(DefaultIntSlice.reuse(idx), s)
    }

    inline operator fun set(vararg indexable: Index, s: E) {
        set(WrappedIndex.of(indexable), s)
    }
}

/**
 * 如果集合不为空，则执行[block]
 */
inline fun <E> RandomAccessCollection<E>.ifAny(block: RandomAccessCollection<E>.() -> Unit) {
    for (element in this) return block()
}