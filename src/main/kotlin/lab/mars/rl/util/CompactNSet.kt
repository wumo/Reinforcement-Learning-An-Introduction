package lab.mars.rl.util

/**
 * <p>
 * Created on 2017-09-18.
 * </p>
 *
 * @author wumo
 */
class CompactNSet<T:Any> : RandomAccessCollection<T>() {
    override fun <T : Any> copycat(element_maker: (IntBuf) -> T): RandomAccessCollection<T> {
        TODO("not implemented")
    }

    override fun <T : Any> _get(idx: Index): T {
        TODO("not implemented")
    }

    override fun <T : Any> _set(idx: Index, s: T) {
        TODO("not implemented")
    }

    override fun indices(): Iterator<IntBuf> {
        TODO("not implemented")
    }

    override fun withIndices(): Iterator<Pair<out IntBuf, T>> {
        TODO("not implemented")
    }


    override fun iterator(): Iterator<T> {
        TODO("not implemented")
    }

}