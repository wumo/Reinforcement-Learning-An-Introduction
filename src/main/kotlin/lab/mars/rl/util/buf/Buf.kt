package lab.mars.rl.util.buf

import lab.mars.rl.util.math.Rand

/**
 * <p>
 * Created on 2017-09-28.
 * </p>
 *
 * @author wumo
 */
interface Buf<T: Any>: Iterable<T> {
  /** [end]>=[start] */
  operator fun get(start: Int, end: Int): Buf<T>
  
  fun toTypedArray(): Array<T>
  fun copy(): Buf<T>
  
  val size: Int
  val isEmpty: Boolean
    get() = size == 0
  val writePtr: Int
    get() = size
  val lastIndex: Int
    get() = size - 1
  val last: T
    get() = get(lastIndex)
  
  /**
   * 获取指定维度[idx]上的数值
   */
  operator fun get(idx: Int): T
  
  fun forEach(start: Int = 0, end: Int = lastIndex, block: (Int, T) -> Unit) {
    for (i in start..end)
      block(i, get(i))
  }
  
  fun equals(other: Buf<T>): Boolean {
    if (this === other) return true
    if (size != other.size) return false
    for (i in 0..lastIndex)
      if (get(i) != other[i]) return false
    return true
  }
  
  override fun iterator() = object: Iterator<T> {
    var a = 0
    override fun hasNext() = a < size
    
    override fun next() = get(a++)
  }
  
  fun rand() = get(Rand().nextInt(size))
}