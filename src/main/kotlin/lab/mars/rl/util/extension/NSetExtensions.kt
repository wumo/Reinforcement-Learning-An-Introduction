package lab.mars.rl.util.extension

import lab.mars.rl.util.*

/**
 * <p>
 * Created on 2017-09-18.
 * </p>
 *
 * @author wumo
 */
fun <T> nsetOf(dimension: Any, element_maker: (IntSlice) -> Any? = { null }): NSet<T> {
    val _dim = dimension.toDim()
    return make(_dim.rootDim, _dim.sub, DefaultIntSlice.new(), element_maker)
}

private val zeroDim = DefaultIntSlice.of(0)
private fun <T> make(rootDim: IntSlice, sub: Array<Dimension>, index: DefaultIntSlice,
                     element_maker: (IntSlice) -> Any? = { null }): NSet<T> {
    val dim = rootDim.toIntArray()
    val isZero = dim.size == 1 && dim[0] == 0
    if (isZero) {
        dim[0] = sub.size
        index.append(0)
    } else
        index.append(dim.size, 0)

    val stride = IntArray(dim.size)
    stride[stride.lastIndex] = 1
    for (a in stride.lastIndex - 1 downTo 0)
        stride[a] = dim[a + 1] * stride[a + 1]

    val total = dim[0] * stride[0]
    return NSet(dim, stride, Array(total) {
        when {
            sub.isEmpty() -> element_maker(index)
            isZero -> make<T>(sub[it].rootDim, sub[it].sub, index, element_maker)
            else -> make<T>(zeroDim, sub, index, element_maker)
        }.apply { index.increment(dim) }
    }.apply { index.removeLast(dim.size) })
}
