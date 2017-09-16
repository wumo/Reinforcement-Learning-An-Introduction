package lab.mars.rl.model.impl

import lab.mars.rl.util.DefaultIntSlice
import lab.mars.rl.util.IntSlice

/**
 * <p>
 * Created on 2017-09-14.
 * </p>
 *
 * @author wumo
 */
operator fun <T> NSet.Companion.invoke(dimension: Dimension, element_maker: (IntSlice) -> Any? = { null }): NSet<T> {
    return make(dimension, DefaultIntSlice.new(), element_maker)
}

val zeroDim = DefaultIntSlice.of(0)
private fun <T> make(dimension: Dimension, index: DefaultIntSlice,
                     element_maker: (IntSlice) -> Any? = { null }): NSet<T> {
    val rootDim = dimension.rootDim
    val sub = dimension.sub
    val dim = rootDim.toIntArray()
    val isZero = dim.size == 1 && dim[0] == 0
    if (isZero) {
        dim[0] = dimension.sub.size
        index.append(0)
    } else
        index.append(rootDim.size, 0)
    val stride = strideOfDim(dim)
    val total = dim[0] * stride[0]
    return NSet(dim, stride, Array(total) {
        when {
            sub.isEmpty() -> element_maker(index)
            isZero -> make<T>(sub[it], index, element_maker)
            else -> make<T>(Dimension(zeroDim, sub), index, element_maker)
        }.apply { index.increment(dim) }
    }.apply { index.removeLast(dim.size) })
}
