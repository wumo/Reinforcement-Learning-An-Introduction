package lab.mars.rl.util.buf

/**
 * <p>
 * Created on 2017-09-28.
 * </p>
 *
 * @author wumo
 */
abstract class MutableIntBuf : IntBuf() {
  abstract val cap: Int
  abstract operator fun set(idx: Int, s: Int)

  /** [end]>=[start] */
  abstract operator fun set(start: Int, end: Int, s: Int)

  abstract fun ensure(minCap: Int)

  abstract fun prepend(s: Int)
  abstract fun prepend(num: Int, s: Int)
  abstract fun prepend(another: Index)

  abstract fun append(s: Int)
  abstract fun append(num: Int, s: Int)
  abstract fun append(another: Index)

  fun remove(range: IntRange) {
    remove(range.start, range.endInclusive)
  }

  /** [end]>=[start] */
  abstract fun remove(start: Int, end: Int)

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

  abstract fun reuseBacked(): IntBuf
  abstract fun append(data: IntArray)
}
