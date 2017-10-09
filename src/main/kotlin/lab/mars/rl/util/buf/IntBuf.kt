package lab.mars.rl.util.buf

import lab.mars.rl.util.Index

/**
 * <p>
 * Created on 2017-09-28.
 * </p>
 *
 * @author wumo
 */
interface IntBuf : Index {
    /** [end]>=[start] */
    operator fun get(start: Int, end: Int): IntBuf

    fun toIntArray(): IntArray
    fun copy(): IntBuf
}