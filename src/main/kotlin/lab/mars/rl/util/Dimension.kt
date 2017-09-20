@file:Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")

package lab.mars.rl.util

/**
 * <p>
 * Created on 2017-09-14.
 * </p>
 *
 * @author wumo
 */

val emptyNSet = NSet<Any>(intArrayOf(), intArrayOf(), Array(0) {})
val NULL_obj = object : Any() {}
val null_element_maker: (IntSlice) -> Any = { NULL_obj }

sealed abstract class Dimension {
    abstract fun <T : Any> nset(index: DefaultIntSlice, element_maker: (IntSlice) -> Any): NSet<T>
}

class VariationalDimension(private val dimFunc: (IntSlice) -> Any) : Dimension() {
    override fun <T : Any> nset(index: DefaultIntSlice, element_maker: (IntSlice) -> Any): NSet<T> {
        val dim = dimFunc(index).toDim()
        return dim.nset(index, element_maker)
    }
}

class EnumeratedDimension(private val enumerated: Array<Dimension>) : Dimension() {
    override fun <T : Any> nset(index: DefaultIntSlice, element_maker: (IntSlice) -> Any): NSet<T> {
        return when {
            enumerated.isEmpty() -> emptyNSet as NSet<T>
            enumerated.size == 1 -> enumerated[0].nset(index, element_maker)
            else -> {
                val dim = intArrayOf(enumerated.size)
                val stride = intArrayOf(1)
                index.append(0)
                NSet<T>(dim, stride, Array(enumerated.size) {
                    enumerated[it].nset<T>(index, element_maker)
                            .apply { index.increment(dim) }
                }).apply { index.removeLast(1) }
            }
        }
    }
}

class GeneralDimension(val dimSlice: DefaultIntSlice, val levels: ArrayList<Dimension>) : Dimension() {
    override fun <T : Any> nset(index: DefaultIntSlice, element_maker: (IntSlice) -> Any): NSet<T> {
        if (dimSlice.size == 1 && dimSlice[0] == 0) {//zero dimension
            if (levels.isEmpty()) return emptyNSet as NSet<T>
            return generateSub(index, element_maker)
        } else {
            val dim = dimSlice.toIntArray()
            val stride = IntArray(dim.size)
            stride[stride.lastIndex] = 1
            for (a in stride.lastIndex - 1 downTo 0)
                stride[a] = dim[a + 1] * stride[a + 1]
            val total = dim[0] * stride[0]
            index.append(dim.size, 0)
            return NSet<T>(dim, stride, Array(total) {
                when {
                    levels.isEmpty() -> element_maker(index)
                    else -> generateSub<T>(index, element_maker)
                }.apply { index.increment(dim) }
            }).apply { index.removeLast(dim.size) }
        }
    }

    private fun <T : Any> generateSub(index: DefaultIntSlice, element_maker: (IntSlice) -> Any): NSet<T> {
        val result = levels[0].nset<T>(index, null_element_maker)

        for (next in 1 until levels.lastIndex)
            result.raw_set { idx, _ ->
                index.append(idx)
                levels[next].nset<T>(index, null_element_maker).apply { index.removeLast(idx.size) }
            }

        result.raw_set { idx, _ ->
            index.append(idx)
            element_maker(idx).apply { index.removeLast(idx.size) }
        }
        return result
    }

}

operator fun Int.invoke(vararg s: Any) =
        GeneralDimension(DefaultIntSlice.of(this), ArrayList()).apply {
            if (s.isNotEmpty())
                levels.add(EnumeratedDimension(Array(s.size) { s[it].toDim() }))
        }

infix fun Int.x(a: Int) = GeneralDimension(DefaultIntSlice.of(this, a), ArrayList())

infix fun Int.x(block: (IntSlice) -> Any) =
        GeneralDimension(DefaultIntSlice.of(this), arrayListOf(VariationalDimension(block)))

infix fun Int.x(d: GeneralDimension) =
        GeneralDimension(DefaultIntSlice.of(this), arrayListOf(d))

operator fun GeneralDimension.invoke(vararg s: Any) =
        this.apply {
            if (s.isNotEmpty())
                levels.add(EnumeratedDimension(Array(s.size) { s[it].toDim() }))
        }

infix fun GeneralDimension.x(a: Int) =
        this.apply {
            if (levels.isNotEmpty()) {
                val last = levels.last()
                if (last is GeneralDimension && last.levels.isEmpty()) {
                    last.dimSlice.append(a)
                    return@apply
                }
            }
            levels.add(GeneralDimension(DefaultIntSlice.of(a), emptySub))
        }

infix fun GeneralDimension.x(block: (IntSlice) -> Any) =
        this.apply { levels.add(VariationalDimension(block)) }


infix fun GeneralDimension.x(d: GeneralDimension) =
        this.apply { levels.add(d) }

fun Any.toDim() = when (this) {
    is Dimension -> this
    is Int -> GeneralDimension(DefaultIntSlice.of(this), emptySub)
    else -> throw IllegalArgumentException(this.toString())
}


val emptySub = ArrayList<Dimension>(0)

fun <T : Any> nsetOf(dimension: Any, element_maker: (IntSlice) -> T): NSet<T> {
    val _dim = dimension.toDim()
    return when (_dim) {
        is GeneralDimension -> make(_dim.dimSlice, _dim.levels, DefaultIntSlice.new(), element_maker)
        else -> throw IllegalArgumentException("${_dim::class} is not supported in creating ${NSet::class} directly!")
    }
}

private val zeroDim = DefaultIntSlice.of(0)
private fun <T : Any> make(rootDim: IntSlice, sub: ArrayList<Dimension>,
                           index: DefaultIntSlice,
                           element_maker: (IntSlice) -> T): NSet<T> {
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

    return when {
        sub.isEmpty() -> NSet<T>(dim, stride, Array(total) {
            element_maker(index).apply { index.increment(dim) }
        })
        isZero || sub.size == 1 -> NSet<T>(dim, stride, Array(total) {
            val tmp = sub[it]
            when (tmp) {
                is VariationalDimension -> {
                    val sub_dim = tmp.dimSlice(index).toDim()

                }
                is EnumeratedDimension -> {

                }
                is GeneralDimension -> {
                }
            }
            make(sub[it].dimSlice, sub[it].dimSlice, index, element_maker).apply { index.increment(dim) }
        })
        else -> make(zeroDim, sub, index, element_maker)
    }.apply { index.removeLast(dim.size) }

//    return NSet(dim
//                , stride, Array(total) {
//        when {
//            sub.isEmpty() -> element_maker(index)
//            isZero -> make(sub[it].dim, sub[it].sub, index, element_maker)
//            sub.size == 1 -> make(sub[0].toDim(), sub[0].sub, index, element_maker)
//            else -> make(zeroDim, sub, index, element_maker)
//        }.apply { index.increment(dim) }
//    }.apply { index.removeLast(dim.size) })
}