package lab.mars.rl.util

/**
 * <p>
 * Created on 2017-09-25.
 * </p>
 *
 * @author wumo
 */
interface ReusableSnapshotIterator<E> : Iterator<E> {
    fun snapshot(): ReusableSnapshotIterator<E>
    fun rewind()
}