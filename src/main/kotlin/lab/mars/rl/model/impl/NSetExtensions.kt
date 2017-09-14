package lab.mars.rl.model.impl

import lab.mars.rl.util.IntSlice

/**
 * <p>
 * Created on 2017-09-14.
 * </p>
 *
 * @author wumo
 */
operator fun <T> NSet.Companion.invoke(dimension: Dimension): NSet<T> {
    val sub = dimension.sub
    val dim = dimension.rootDim.toIntArray()
    val isZero = dim.size == 1 && dim[0] == 0
    if (isZero) dim[0] = dimension.sub.size
    val stride = strideOfDim(dim)
    val total = dim[0] * stride[0]
    return NSet<T>(dim, stride, Array(total) {
        when {
            sub.isEmpty() -> null
            isZero -> NSet<T>(sub[it])
            else ->
                NSet<T>(Dimension(IntSlice.of(sub.size), sub))
        }
    })
}
