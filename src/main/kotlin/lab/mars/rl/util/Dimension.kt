@file:Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY", "NOTHING_TO_INLINE")

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
val `placeholder's recipe`: (IntBuf) -> Any = { NULL_obj }

private fun strideOfDim(dim: IntArray): IntArray {
    val stride = IntArray(dim.size)
    stride[stride.lastIndex] = 1
    for (a in stride.lastIndex - 1 downTo 0)
        stride[a] = dim[a + 1] * stride[a + 1]
    return stride
}

sealed class Dimension {
    abstract fun <T : Any> fill(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): Any
}

class VariationalDimension(private val dimFunc: (IntBuf) -> Any) : Dimension() {
    override fun <T : Any> fill(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): Any {
        val dim = dimFunc(slot).toDim()
        return dim.fill<T>(slot, recipe)
    }
}

inline fun <T> Array<T>.isSingle() = this.size == 1
class EnumeratedDimension(private val enumerated: Array<Dimension>) : Dimension() {
    override fun <T : Any> fill(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): Any {
        return when {
            enumerated.isEmpty() -> recipe(slot)
            enumerated.isSingle() -> enumerated.single().fill<T>(slot, recipe)
            else -> {
                val dim = intArrayOf(enumerated.size)
                val stride = intArrayOf(1)
                slot.append(0)//extend to subtree
                NSet<T>(dim, stride, Array(enumerated.size) {
                    enumerated[it].fill<T>(slot, recipe)
                            .apply { slot.increment(dim) }
                }).apply { slot.removeLast(1) }
            }
        }
    }
}

inline fun DefaultIntBuf.isZero() = this.size == 1 && this[0] == 0
class GeneralDimension(val dimSlice: DefaultIntBuf, val levels: ArrayList<Dimension>) : Dimension() {
    override fun <T : Any> fill(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): Any {
        return if (dimSlice.isZero()) {//zero dimension
            cascade<T>(slot, recipe)//to sibling slot
        } else {
            val dim = dimSlice.toIntArray()
            val stride = strideOfDim(dim)
            slot.append(dim.size, 0)//to children slot
            NSet<T>(dim, stride, Array(dim[0] * stride[0]) {
                //to sibling slot
                cascade<T>(slot, recipe).apply {
                    slot.increment(dim)
                }
            }).apply {
                slot.removeLast(dim.size)//to parent slot
            }
        }
    }

    private fun <T : Any> cascade(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): Any {
        if (levels.isEmpty()) return recipe(slot)
        val tree = levels[0].fill<T>(slot, `placeholder's recipe`)
        if (tree !is NSet<*>) return tree
        //grow branches
        for (level in 1..levels.lastIndex)
            tree.set { idx, _ ->
                slot.append(idx)
                levels[level].fill<T>(slot, `placeholder's recipe`).apply {
                    slot.removeLast(idx.size)
                }
            }

        //attach leaves
        tree.set { idx, _ ->
            slot.append(idx)
            recipe(idx).apply {
                slot.removeLast(idx.size)
            }
        }
        return tree
    }
}

private fun DefaultIntBuf.simplifyLast(): DefaultIntBuf {
    while (this.size >= 2) {
        val last1 = this[lastIndex]
        val last2 = this[lastIndex - 1]
        if (last1 == 0 || last2 == 0) {
            removeLast(1)
            this[lastIndex] = last1 + last2
            continue
        }
        break
    }
    return this
}

operator fun Int.invoke(vararg s: Any): GeneralDimension {
    require(this >= 0)
    return GeneralDimension(DefaultIntBuf.of(this), ArrayList()).apply {
        if (s.isNotEmpty()) {
            if (s.isSingle())
                levels.add(s.first().toDim())
            else
                levels.add(EnumeratedDimension(Array(s.size) { s[it].toDim() }))
        }
    }
}

infix fun Int.x(a: Int): GeneralDimension {
    require(this >= 0 && a >= 0)
    return GeneralDimension(DefaultIntBuf.of(this, a).simplifyLast(), ArrayList())
}

infix fun Int.x(block: (IntBuf) -> Any): GeneralDimension {
    require(this >= 0)
    return GeneralDimension(DefaultIntBuf.of(this), arrayListOf(VariationalDimension(block)))
}

infix fun Int.x(d: GeneralDimension): GeneralDimension {
    require(this >= 0)
    return GeneralDimension(DefaultIntBuf.of(this), arrayListOf(d))
}

operator fun GeneralDimension.invoke(vararg s: Any) =
        this.apply {
            if (s.isNotEmpty())
                levels.add(EnumeratedDimension(Array(s.size) { s[it].toDim() }))
        }

infix fun GeneralDimension.x(a: Int): GeneralDimension {
    require(a >= 0)
    return this.apply {
        if (levels.isNotEmpty()) {
            val last = levels.last()
            if (last is GeneralDimension && last.levels.isEmpty()) {//simplification
                last.dimSlice.apply {
                    append(a)
                    simplifyLast()
                }
                return@apply
            }
        }
        levels.add(GeneralDimension(DefaultIntBuf.of(a), emptyLevels))
    }
}

infix fun GeneralDimension.x(block: (IntBuf) -> Any) =
        this.apply { levels.add(VariationalDimension(block)) }


infix fun GeneralDimension.x(d: GeneralDimension) =
        this.apply {
            when {
                levels.isEmpty() -> {

                }

            }
            levels.add(d)
        }

fun Any.toDim() = when (this) {
    is Dimension -> this
    is Int -> {
        require(this >= 0)
        GeneralDimension(DefaultIntBuf.of(this), emptyLevels)
    }
    else -> throw IllegalArgumentException(this.toString())
}


val emptyLevels = ArrayList<Dimension>(0)

fun <T : Any> nsetOf(dimension: Any, element_maker: (IntBuf) -> T): NSet<T> {
    val _dim = dimension.toDim()
    return when (_dim) {
        is GeneralDimension -> fill(_dim.dimSlice, _dim.levels, DefaultIntBuf.new(), element_maker)
        else -> throw IllegalArgumentException("${_dim::class} is not supported in creating ${NSet::class} directly!")
    }
}

private val zeroDim = DefaultIntBuf.of(0)
private fun <T : Any> fill(rootDim: IntBuf, sub: ArrayList<Dimension>,
                           index: DefaultIntBuf,
                           element_maker: (IntBuf) -> T): NSet<T> {
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
            fill(sub[it].dimSlice, sub[it].dimSlice, index, element_maker).apply { index.increment(dim) }
        })
        else -> fill(zeroDim, sub, index, element_maker)
    }.apply { index.removeLast(dim.size) }

//    return NSet(dim
//                , stride, Array(total) {
//        when {
//            sub.isEmpty() -> element_maker(index)
//            isZero -> fill(sub[it].dim, sub[it].sub, index, element_maker)
//            sub.size == 1 -> fill(sub[0].toDim(), sub[0].sub, index, element_maker)
//            else -> fill(zeroDim, sub, index, element_maker)
//        }.apply { index.increment(dim) }
//    }.apply { index.removeLast(dim.size) })
}