package lab.mars.rl.util.buf

/**
 * <p>
 * Created on 2017-09-28.
 * </p>
 *
 * @author wumo
 */
abstract class IntBuf: Index() {
  /** [end]>=[start] */
  abstract operator fun get(start: Int, end: Int): IntBuf
  
  abstract fun toIntArray(): IntArray
  abstract fun copy(): IntBuf
}