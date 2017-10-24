package lab.mars.rl.util.buf

import lab.mars.rl.util.Index

/**
 * <p>
 * Created on 2017-09-28.
 * </p>
 *
 * @author wumo
 */
interface MutableIntBuf : IntBuf {
    val cap: Int
    operator fun set(idx: Int, s: Int)

    /** [end]>=[start] */
    operator fun set(start: Int, end: Int, s: Int)

    fun ensure(minCap: Int)

    fun prepend(s: Int)
    fun prepend(num: Int, s: Int)
    fun prepend(another: Index)

    fun append(s: Int)
    fun append(num: Int, s: Int)
    fun append(another: Index)

    fun remove(range: IntRange) {
        remove(range.start, range.endInclusive)
    }

    /** [end]>=[start] */
    fun remove(start: Int, end: Int)

    fun remove(index: Int) = remove(index, index)
    fun removeFirst(num: Int) {
        if (num == 0) return
        remove(0, num - 1)
    }

    fun removeLast(num: Int) {
        if (num == 0) return
        remove(lastIndex - num + 1, lastIndex)
    }

    fun clear() {
        removeLast(size)
    }

    fun reuseBacked(): IntBuf
}
