@file:Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY", "NOTHING_TO_INLINE")

package lab.mars.rl.util

/**
 * <p>
 * Created on 2017-09-14.
 * </p>
 *
 * @author wumo
 */
val emptyLevels = ArrayList<Dimension>(0)
val NULL_obj = object : Any() {}
val `placeholder's recipe`: (IntBuf) -> Any = { NULL_obj }

private fun strideOfDim(dim: IntArray): IntArray {
    val stride = IntArray(dim.size)
    stride[stride.lastIndex] = 1
    for (a in stride.lastIndex - 1 downTo 0)
        stride[a] = dim[a + 1] * stride[a + 1]
    return stride
}

fun <E : Any> nsetFrom(dim: Dimension, recipe: (IntBuf) -> E) = dim.NSet(recipe)
fun <E : Any> nsetFrom(dim: Int, recipe: (IntBuf) -> E) = dim.NSet(recipe)

sealed class Dimension {
    internal abstract fun <E : Any> fill(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E>
    open fun <E : Any> NSet(recipe: (IntBuf) -> E) =
            fill<E>(DefaultIntBuf.new(), recipe)
}

class ExpandDimension(val dim: Int) : Dimension() {
    override fun <E : Any> fill(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E>
            = dim.toDim().fill(slot, recipe)
}

class VariationalDimension(private val dimFunc: (IntBuf) -> Any) : Dimension() {
    override fun <E : Any> fill(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E> {
        val dim = dimFunc(slot).toDim()
        return dim.fill(slot, recipe)
    }
}

inline fun <T> Array<T>.isSingle() = this.size == 1
class EnumeratedDimension(private val enumerated: Array<Dimension>) : Dimension() {
    override fun <E : Any> fill(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E> {
        return when {
            enumerated.isEmpty() -> emptyNSet()
            enumerated.isSingle() -> enumerated.single().fill(slot, recipe)
            else -> {
                val total = enumerated.sumBy { (it as? ExpandDimension)?.dim ?: 1 }
                val dim = intArrayOf(total)
                val stride = intArrayOf(1)
                slot.append(0)//extend to subtree
                val array = Array<Any>(total) {}
                var i = 0
                for (dimension in enumerated)
                    when (dimension) {
                        is ExpandDimension ->
                            repeat(dimension.dim) {
                                array[i++] = recipe(slot)
                                slot.increment(dim)
                            }
                        else -> {
                            array[i++] = dimension.fill<E>(slot, recipe)
                            slot.increment(dim)
                        }
                    }
                NSet<E>(dim, stride, array).apply { slot.removeLast(1) }
            }
        }
    }
}

inline fun DefaultIntBuf.isZero() = this.size == 1 && this[0] == 0
class GeneralDimension(internal val dim: DefaultIntBuf, internal val levels: ArrayList<Dimension>) : Dimension() {
    override fun <E : Any> fill(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E> {
        return if (dim.isZero()) {//zero dimension
            if (levels.isEmpty()) emptyNSet()
            else cascade(slot, recipe)//to sibling slot
        } else {
            val dim = dim.toIntArray()
            val stride = strideOfDim(dim)
            slot.append(dim.size, 0)//to children slot
            NSet<E>(dim, stride, Array(dim[0] * stride[0]) {
                when {
                    levels.isEmpty() -> recipe(slot)
                    else -> cascade<E>(slot, recipe)//to sibling slot
                }.apply { slot.increment(dim) }
            }).apply { slot.removeLast(dim.size) }//to parent slot
        }
    }

    override fun <E : Any> NSet(recipe: (IntBuf) -> E): NSet<E> {
        if (dim.isZero() && levels.isEmpty()) return emptyNSet()
        return super.NSet(recipe)
    }

    private fun <E : Any> cascade(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E> {
        val tree = levels[0].fill<E>(slot, `placeholder's recipe`)
        //grow branches
        for (level in 1..levels.lastIndex)
            tree.set { idx, _ ->
                slot.append(idx)
                levels[level].fill<E>(slot, `placeholder's recipe`).apply {
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
            remove(lastIndex)
            this[lastIndex] = last1 + last2
            continue
        }
        break
    }
    return this
}

private fun DefaultIntBuf.simplifyFirst(): DefaultIntBuf {
    while (this.size >= 2) {
        val last1 = this[0]
        val last2 = this[1]
        if (last1 == 0 || last2 == 0) {
            remove(0)
            this[0] = last1 + last2
            continue
        }
        break
    }
    return this
}

operator fun Int.not(): ExpandDimension {
    require(this >= 0)
    return ExpandDimension(this)
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

fun <E : Any> Int.NSet(recipe: (IntBuf) -> E) =
        GeneralDimension(DefaultIntBuf.of(this), emptyLevels).NSet(recipe)

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
    return d.apply {
        dim.prepend(this@x)
        dim.simplifyFirst()
    }
}

operator fun GeneralDimension.invoke(vararg s: Any) =
        this.apply {
            if (s.isNotEmpty())
                levels.add(EnumeratedDimension(Array(s.size) { s[it].toDim() }))
        }

infix fun GeneralDimension.x(a: Int): GeneralDimension {
    require(a >= 0)
    return this.apply {
        if (levels.isEmpty())
            dim.apply {
                append(a)
                simplifyLast()
            }
        else {
            val last = levels.last()
            if (last is GeneralDimension && last.levels.isEmpty()) {//simplification
                last.dim.apply {
                    append(a)
                    simplifyLast()
                }
                return@apply
            }
            levels.add(GeneralDimension(DefaultIntBuf.of(a), emptyLevels))
        }
    }
}

infix fun GeneralDimension.x(block: (IntBuf) -> Any) =
        this.apply { levels.add(VariationalDimension(block)) }


infix fun GeneralDimension.x(d: GeneralDimension): GeneralDimension {
    val first = this
    val second = d
    when {
        first.levels.isEmpty() -> {
            second.dim.prepend(first.dim)
            second.dim.simplifyLast().simplifyFirst()
            return second
        }
        else -> {
            val last = first.levels.last()
            if (last is GeneralDimension && last.levels.isEmpty()) {//simplification
                last.dim.apply {
                    append(second.dim)
                    simplifyLast().simplifyFirst()
                }
                first.levels.addAll(second.levels)
            } else
                first.levels.add(d)
            return first
        }
    }
}

fun Any.toDim() = when (this) {
    is Dimension -> this
    is Int -> {
        require(this >= 0)
        GeneralDimension(DefaultIntBuf.of(this), emptyLevels)
    }
    else -> throw IllegalArgumentException(this.toString())
}

