package lab.mars.rl.util.buf

import lab.mars.rl.util.Index

/**
 * <p>
 * Created on 2017-09-28.
 * </p>
 *
 * @author wumo
 */
abstract class IntBuf : Index() {
    /** [end]>=[start] */
    abstract operator fun get(start: Int, end: Int): IntBuf

    abstract fun toIntArray(): IntArray
    abstract fun copy(): IntBuf
}