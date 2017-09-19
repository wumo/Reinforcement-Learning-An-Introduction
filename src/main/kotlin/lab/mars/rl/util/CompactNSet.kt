package lab.mars.rl.util

/**
 * <p>
 * Created on 2017-09-18.
 * </p>
 *
 * @author wumo
 */
class CompactNSet<T> : RandomAccessCollection<T>() {
    override fun indices(): Iterator<IntSlice> {
        TODO("not implemented")
    }

    override fun withIndices(): Iterator<Pair<out IntSlice, T>> {
        TODO("not implemented")
    }

    override fun <T> _get(idx: Index): T {
        TODO("not implemented")
    }

    override fun <T> _set(idx: Index, s: T) {
        TODO("not implemented")
    }

    override fun iterator(): Iterator<T> {
        TODO("not implemented")
    }

}