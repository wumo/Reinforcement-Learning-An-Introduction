package lab.mars.rl.util.buf

/**
 * <p>
 * Created on 2017-09-28.
 * </p>
 *
 * @author wumo
 */
interface MutableBuf<T : Any> : Buf<T> {
    val cap: Int

    override fun get(start: Int, end: Int): MutableBuf<T>

    operator fun set(idx: Int, s: T)

    /** [end]>=[start] */
    operator fun set(start: Int, end: Int, s: T)

    fun unfold(num: Int)

    fun ensure(minCap: Int)

    fun prepend(s: T)
    fun prepend(num: Int, s: T)
    fun prepend(another: Buf<T>)

    fun append(s: T)
    fun append(num: Int, s: T)
    fun append(another: Buf<T>)

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

    fun reuseBacked(): Buf<T>
}