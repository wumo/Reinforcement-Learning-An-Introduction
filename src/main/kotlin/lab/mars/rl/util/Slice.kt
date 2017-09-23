@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util

class Slice<T>(val backed: Array<T>, val start: Int, val end: Int) {
    init {
        require(start in 0..end)
        require(end < backed.size)
    }

    val size = end - start + 1
    val lastIndex = size - 1
    inline operator fun get(idx: Int): T {
        require(idx in 0..lastIndex)
        return backed[start + idx]
    }

    inline operator fun set(idx: Int, s: T) {
        require(idx in 0..lastIndex)
        backed[start + idx] = s
    }
}