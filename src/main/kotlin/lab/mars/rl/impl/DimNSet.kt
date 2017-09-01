package lab.mars.rl.impl

import lab.mars.rl.IndexedCollection

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
class DimNSet<E> constructor(private val dim: IntArray, private val stride: IntArray, val raw: Array<E>) : IndexedCollection<IntArray, E> {
    companion object {
        operator inline fun <reified T> invoke(dim: IntArray, element_maker: (IntArray) -> T): DimNSet<T> {
            val stride = IntArray(dim.size)
            stride[stride.size - 1] = 1
            for (a in stride.size - 2 downTo 0)
                stride[a] = dim[a + 1] * stride[a + 1]
            val total = dim[0] * stride[0]
            val index = IntArray(dim.size)
            val raw = Array(total, {
                element_maker(index).apply {
                    for (idx in index.size - 1 downTo 0) {
                        index[idx]++
                        if (index[idx] < dim[idx])
                            break
                        index[idx] = 0
                    }
                }
            })
            return DimNSet(dim, stride, raw)
        }
    }

    private fun offset(idx: IntArray): Int {
        var offset = 0
        if (idx.size != dim.size)
            throw RuntimeException("index.length=${idx.size}  > dim.length=${dim.size}")
        for ((a, _idx) in idx.withIndex()) {
            if (_idx < 0 || _idx > dim[a])
                throw ArrayIndexOutOfBoundsException("index[$a]= $_idx while dim[$a]=${dim[a]}")
            offset += idx[a] * stride[a]
        }
        return offset
    }

    override fun get(index: IntArray) = raw[offset(index)]

    override fun set(index: IntArray, s: E) {
        raw[offset(index)] = s
    }

    override fun iterator(): Iterator<E> {
    }

}