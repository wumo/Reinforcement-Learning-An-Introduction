@file:Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY", "NOTHING_TO_INLINE", "NAME_SHADOWING")

package lab.mars.rl.util

import lab.mars.rl.util.CompactNSet.Cell
import lab.mars.rl.util.CompactNSet.SubTree
import lab.mars.rl.util.Bufkt.*
import java.util.*

/**
 * <p>
 * Created on 2017-09-14.
 * </p>
 *
 * @author wumo
 */
val emptyLevels = LinkedList<Dimension>()
val NULL_obj = object : Any() {}
val `placeholder's recipe`: (IntBuf) -> Any = { NULL_obj }

fun strideOfDim(dim: IntArray): IntArray {
    val stride = IntArray(dim.size)
    stride[stride.lastIndex] = 1
    for (a in stride.lastIndex - 1 downTo 0)
        stride[a] = dim[a + 1] * stride[a + 1]
    return stride
}

fun <E : Any> nsetFrom(dim: Dimension, recipe: (IntBuf) -> E) =
        dim.NSet(recipe)

fun <E : Any> nsetFrom(dim: Int, recipe: (IntBuf) -> E) =
        dim.NSet(recipe)

fun <E : Any> cnsetFrom(dim: Dimension, recipe: (IntBuf) -> E) =
        dim.CNSet(recipe)

fun <E : Any> cnsetFrom(dim: Int, recipe: (IntBuf) -> E) =
        dim.CNSet(recipe)

interface levelsIterator : Iterator<Dimension> {
    fun snapshot(): levelsIterator
    fun rewind()
}

val emptyLevelsIterator = object : levelsIterator {
    override fun snapshot() = this

    override fun rewind() {}

    override fun hasNext() = false

    override fun next() = throw NoSuchElementException()
}

fun <E : Any> Int.NSet(recipe: (IntBuf) -> E) =
        GeneralDimension(DefaultIntBuf.of(this), emptyLevels).NSet(recipe)


fun <E : Any> Int.CNSet(recipe: (IntBuf) -> E) =
        GeneralDimension(DefaultIntBuf.of(this), emptyLevels).CNSet(recipe)


sealed class Dimension {
    abstract fun <E : Any> NSet(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E>

    open fun <E : Any> NSet(recipe: (IntBuf) -> E): NSet<E> {
        return NSet(DefaultIntBuf.new(), recipe)
    }

    abstract fun sum(slot: DefaultIntBuf = DefaultIntBuf.new(), successors: levelsIterator): Int

    abstract fun <E : Any> CNSet(offset: Int, slot: MutableIntBuf, set: CompactNSet<E>)

    fun <E : Any> CNSet(size: Int, slot: MutableIntBuf = DefaultIntBuf.new(), recipe: (IntBuf) -> E): CompactNSet<E> {
        val data = Array(size) { NULL_obj }.buf(0, 0)
        val set = CompactNSet<E>(data)
        slot.clear()
        CNSet(0, slot, set)
        slot.clear()
        set.dfs(0, 0, slot) { slot, offset ->
            set._set(offset, recipe(slot))
        }
        return set
    }

    open fun <E : Any> CNSet(recipe: (IntBuf) -> E)
            : CompactNSet<E> {
        val slot = DefaultIntBuf.new()
        val size = sum(slot, emptyLevelsIterator)
        return CNSet(size, slot, recipe)
    }
}

class ExpandDimension(internal val dim: Int) : Dimension() {
    override fun <E : Any> NSet(slot: DefaultIntBuf, recipe: (IntBuf) -> Any)
            = dim.toDim().NSet<E>(slot, recipe)

    override fun sum(slot: DefaultIntBuf, successors: levelsIterator)
            = dim.toDim().sum(slot, successors)

    override fun <E : Any> CNSet(offset: Int, slot: MutableIntBuf, set: CompactNSet<E>)
            = dim.toDim().CNSet(offset, slot, set)
}

class VariationalDimension(private val dimFunc: (IntBuf) -> Any)
    : Dimension() {

    override fun <E : Any> NSet(slot: DefaultIntBuf,
                                recipe: (IntBuf) -> Any)
            = dimFunc(slot).toDim().NSet<E>(slot, recipe)

    override fun sum(slot: DefaultIntBuf, successors: levelsIterator)
            = dimFunc(slot).toDim().sum(slot, successors)

    override fun <E : Any> CNSet(offset: Int, slot: MutableIntBuf, set: CompactNSet<E>)
            = dimFunc(slot).toDim().CNSet(offset, slot, set)
}

inline fun <T> Array<T>.isSingle() = this.size == 1
class EnumeratedDimension
constructor(private val enumerated: Array<Dimension>)
    : Dimension() {
    override fun <E : Any> NSet(slot: DefaultIntBuf,
                                recipe: (IntBuf) -> Any): NSet<E> {
        return when {
            enumerated.isEmpty() -> emptyNSet()
            enumerated.isSingle() -> enumerated.single().NSet(slot, recipe)
            else -> {
                val total = enumerated.sumBy {
                    (it as? ExpandDimension)?.dim ?: 1
                }
                val dim = intArrayOf(total)
                val stride = intArrayOf(1)
                val array = Array<Any>(total) {}
                var i = 0
                slot.append(0)//extend to subtree
                for (dimension in enumerated)
                    when (dimension) {
                        is ExpandDimension ->
                            repeat(dimension.dim) {
                                array[i++] = recipe(slot)
                                slot.increment(dim)
                            }
                        else -> {
                            array[i++] = dimension.NSet<E>(slot, recipe)
                            slot.increment(dim)
                        }
                    }
                NSet<E>(dim, stride, array).apply {
                    slot.removeLast(1)
                }
            }
        }
    }

    override fun sum(slot: DefaultIntBuf, successors: levelsIterator): Int {
        return when {
            enumerated.isEmpty() ->
                if (successors.hasNext())
                    successors.next().sum(slot, successors)
                else 0
            enumerated.isSingle() -> enumerated.single().sum(slot, successors)
            else -> {
                val total = enumerated.sumBy {
                    (it as? ExpandDimension)?.dim ?: 1
                }
                val dim = intArrayOf(total)
                var count = 0
                slot.append(0)
                for (dimension in enumerated)
                    when (dimension) {
                        is ExpandDimension ->
                            when {
                                successors.hasNext() -> repeat(dimension.dim) {
                                    val succ = successors.snapshot()
                                    count += succ.next().sum(slot, succ)
                                    slot.increment(dim)
                                }
                                else -> {
                                    count += dimension.dim
                                    slot[slot.lastIndex] += dimension.dim
                                }
                            }
                        else -> {
                            count += dimension.sum(slot, successors.snapshot())
                            slot.increment(dim)
                        }
                    }
                count.apply { slot.removeLast(1) }
            }
        }
    }

    override fun <E : Any> CNSet(offset: Int, slot: MutableIntBuf, set: CompactNSet<E>) {
        when {
            enumerated.isEmpty() -> return
            enumerated.isSingle() -> enumerated.single().CNSet(offset, slot, set)
            else -> {
                val total = enumerated.sumBy {
                    (it as? ExpandDimension)?.dim ?: 1
                }
                if (total == 0) return

                val tmp = set.data[offset] as? Cell<E>
                          ?: Cell(DefaultBuf.new(), set.data[offset] as E)
                val subtree = SubTree(size = total,
                                      offset2nd = set.data.size)
                tmp.subtrees += subtree
                set.data[offset] = tmp
                set.data.unfold(total - 1)
                //leaf node
                var firstSlot = true
                slot.append(0)
                outer@ for (dimension in enumerated)
                    when (dimension) {
                        is ExpandDimension -> {
                            if (dimension.dim == 0) continue@outer
                            slot[slot.lastIndex] += dimension.dim
                            if (firstSlot) firstSlot = false
                        }
                        else -> {
                            if (firstSlot) {
                                firstSlot = false
                                dimension.CNSet(offset, slot, set)
                            } else {
                                val data_offset = subtree.offset2nd + slot[slot.lastIndex] - 1
                                dimension.CNSet(data_offset, slot, set)
                            }
                            slot[slot.lastIndex]++
                        }
                    }
                slot.removeLast(1)
            }
        }
    }
}

inline fun DefaultIntBuf.isZero() = this.size == 1 && this[0] == 0
class GeneralDimension(
        internal val dim: DefaultIntBuf,
        internal val levels: LinkedList<Dimension>) : Dimension() {
    override fun <E : Any> NSet(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E> {
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

    private fun <E : Any> cascade(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E> {
        val tree = levels.first().NSet<E>(slot, `placeholder's recipe`)
        //grow branches
        for (level in 1..levels.lastIndex)
            tree.raw_set { idx, _ ->
                slot.append(idx)
                levels[level].NSet<E>(slot, `placeholder's recipe`).apply {
                    slot.removeLast(idx.size)
                }
            }

        //attach leaves
        tree.raw_set { idx, _ ->
            slot.append(idx)
            recipe(slot).apply {
                slot.removeLast(idx.size)
            }
        }
        return tree
    }

    private inner class levelsItr
    constructor(val start: Int, val successors: levelsIterator)
        : levelsIterator {
        var a = start
        override fun hasNext(): Boolean {
            if (a < levels.size) return true
            return successors.hasNext()
        }

        override fun next(): Dimension {
            if (a < levels.size) return levels[a++]
            return successors.next()
        }

        override fun snapshot() = levelsItr(a, successors.snapshot())

        override fun rewind() {
            a = start
            successors.rewind()
        }
    }

    override fun sum(slot: DefaultIntBuf, successors: levelsIterator): Int {
        val successors = levelsItr(0, successors)
        return if (dim.isZero()) {
            cascadeSum(slot, successors)
        } else {
            val dim = dim.toIntArray()
            val stride = strideOfDim(dim)
            val total = dim[0] * stride[0]
            return when {
                !successors.hasNext() -> total
                else -> {
                    slot.append(dim.size, 0)
                    var sum = 0
                    for (a in 0 until total)
                        sum += cascadeSum(slot, successors.snapshot())
                                .apply { slot.increment(dim) }
                    slot.removeLast(dim.size)
                    sum
                }
            }
        }
    }

    private inline fun cascadeSum(slot: DefaultIntBuf, successors: levelsIterator)
            = if (successors.hasNext()) successors.next().sum(slot, successors) else 0

    override fun <E : Any> CNSet(offset: Int, slot: MutableIntBuf, set: CompactNSet<E>) {
        val end = (set.data[offset] as? Cell<E>)?.subtrees?.size ?: 0
        if (dim.isZero()) {
            if (levels.isEmpty()) return
            cascade2(offset, end, slot, levels, set)
        } else {
            for (d in dim) {
                if (d == 0) continue
                set.dfs(offset, end, slot) { slot, offset ->
                    val tmp = set.data[offset] as? Cell<E>
                              ?: Cell(DefaultBuf.new(), set.data[offset] as E)
                    val subtree = SubTree(size = d,
                                          offset2nd = set.data.size)
                    tmp.subtrees += subtree
                    set.data[offset] = tmp
                    set.data.unfold(d - 1)
                }
            }
            cascade2(offset, end, slot, levels, set)
        }
    }

    private fun <E : Any> cascade2(offset: Int, end: Int, slot: MutableIntBuf,
                                   levels: LinkedList<Dimension>,
                                   set: CompactNSet<E>) {
        if (levels.isEmpty()) return
        for (dimension in levels)
            set.dfs(offset, end, slot) { slot, offset ->
                dimension.CNSet(offset, slot, set)
            }
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
    for (e in e)
        result.addLast(e)
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

