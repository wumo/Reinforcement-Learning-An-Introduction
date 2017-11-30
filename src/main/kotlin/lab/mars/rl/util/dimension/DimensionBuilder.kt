package lab.mars.rl.util.dimension

import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.buf.IntBuf
import java.util.*

/**
 * <p>
 * Created on 2017-09-29.
 * </p>
 *
 * @author wumo
 */

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
    return GeneralDimension(DefaultIntBuf.of(this), LinkedList()).apply {
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
    return GeneralDimension(DefaultIntBuf.of(this, a).simplifyLast(), LinkedList())
}

fun <E> linkedListOf(vararg e: E): LinkedList<E> {
    val result = LinkedList<E>()
    for (_e in e)
        result.addLast(_e)
    return result
}

infix fun Int.x(block: (IntBuf) -> Any): GeneralDimension {
    require(this >= 0)
    return GeneralDimension(DefaultIntBuf.of(this), linkedListOf(VariationalDimension(block)))
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
