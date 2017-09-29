@file:Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY", "NOTHING_TO_INLINE", "NAME_SHADOWING")

package lab.mars.rl.util.dimension

import lab.mars.rl.util.*
import lab.mars.rl.util.Bufkt.DefaultIntBuf
import lab.mars.rl.util.Bufkt.IntBuf
import lab.mars.rl.util.Bufkt.MutableIntBuf
import lab.mars.rl.util.Bufkt.buf
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
}

val emptyLevelsIterator = object : levelsIterator {
    override fun snapshot() = this

    override fun hasNext() = false

    override fun next() = throw NoSuchElementException()
}

fun <E : Any> Int.NSet(recipe: (IntBuf) -> E) =
        GeneralDimension(DefaultIntBuf.of(this), emptyLevels).NSet(recipe)

fun <E : Any> Int.CNSet(recipe: (IntBuf) -> E) =
        GeneralDimension(DefaultIntBuf.of(this), emptyLevels).CNSet(recipe)

sealed class Dimension {
    abstract fun <E : Any> NSet(slot: DefaultIntBuf, recipe: (IntBuf) -> Any): NSet<E>

    fun <E : Any> NSet(recipe: (IntBuf) -> E): NSet<E> {
        return NSet(DefaultIntBuf.new(), recipe)
    }

    abstract fun sum(slot: MutableIntBuf = DefaultIntBuf.new(), successors: levelsIterator = emptyLevelsIterator): Int

    /**
     * DFS的构建方式
     */
    abstract fun <E : Any> CNSet(set: CompactNSet<E>, offset: Int,
                                 slot: MutableIntBuf = DefaultIntBuf.new(),
                                 successors: levelsIterator = emptyLevelsIterator,
                                 recipe: (IntBuf) -> E)

    fun <E : Any> CNSet(size: Int, slot: MutableIntBuf = DefaultIntBuf.new(), recipe: (IntBuf) -> E): CompactNSet<E> {
        if (size == 0)
            return emptyCNSet()
        val data = Array(size) { NULL_obj }.buf(0, 0)
        val set = CompactNSet<E>(data)
        slot.clear()
        CNSet(set, 0, slot, emptyLevelsIterator, recipe)
        set.size
        return set
    }

    fun <E : Any> CNSet(recipe: (IntBuf) -> E): CompactNSet<E> {
        val slot = DefaultIntBuf.new()
        val size = sum(slot, emptyLevelsIterator)
        if (size == 0)
            return emptyCNSet()
        return CNSet(size, slot, recipe)
    }

}

class ExpandDimension(internal val dim: Int) : Dimension() {
    override fun <E : Any> NSet(slot: DefaultIntBuf, recipe: (IntBuf) -> Any)
            = dim.toDim().NSet<E>(slot, recipe)

    override fun sum(slot: MutableIntBuf, successors: levelsIterator)
            = dim.toDim().sum(slot, successors)

    override fun <E : Any> CNSet(set: CompactNSet<E>, offset: Int,
                                 slot: MutableIntBuf, successors: levelsIterator,
                                 recipe: (IntBuf) -> E)
            = dim.toDim().CNSet(set, offset, slot, successors, recipe)
}

class VariationalDimension(private val dimFunc: (IntBuf) -> Any)
    : Dimension() {

    override fun <E : Any> NSet(slot: DefaultIntBuf,
                                recipe: (IntBuf) -> Any)
            = dimFunc(slot).toDim().NSet<E>(slot, recipe)

    override fun sum(slot: MutableIntBuf, successors: levelsIterator)
            = dimFunc(slot).toDim().sum(slot, successors)

    override fun <E : Any> CNSet(set: CompactNSet<E>, offset: Int,
                                 slot: MutableIntBuf, successors: levelsIterator,
                                 recipe: (IntBuf) -> E)
            = dimFunc(slot).toDim().CNSet(set, offset, slot, successors, recipe)
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

    override fun sum(slot: MutableIntBuf, successors: levelsIterator): Int {
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

    override fun <E : Any> CNSet(set: CompactNSet<E>, offset: Int,
                                 slot: MutableIntBuf, successors: levelsIterator,
                                 recipe: (IntBuf) -> E) {
        when {
            enumerated.isEmpty() ->
                if (successors.hasNext())
                    successors.next().sum(slot, successors)
            enumerated.isSingle() ->
                enumerated.single().CNSet(set, offset, slot, successors, recipe)
            else -> {
                val total = enumerated.sumBy {
                    (it as? ExpandDimension)?.dim ?: 1
                }
                if (total == 0) return
                val subtree = set.expand(offset, total)
                fun _offset(): Int {
                    val idx = slot[slot.lastIndex]
                    return if (idx == 0) offset else subtree.offset2nd + idx - 1
                }
                slot.append(0)
                for (dimension in enumerated)
                    when (dimension) {
                        is ExpandDimension ->
                            repeat(dimension.dim) {
                                if (successors.hasNext()) {
                                    val succ = successors.snapshot()
                                    succ.next().CNSet(set, _offset(), slot, succ, recipe)
                                } else
                                    set._set(_offset(), recipe(slot))
                                slot[slot.lastIndex]++
                            }
                        else -> {
                            val succ = successors.snapshot()
                            dimension.CNSet(set, _offset(), slot, succ, recipe)
                            slot[slot.lastIndex]++
                        }
                    }
                slot.removeLast(1)
                subtree.offsetEnd = set.data.writePtr - 1
            }
        }
    }
}

inline fun MutableIntBuf.isZero() = this.size == 1 && this[0] == 0
class GeneralDimension(
        internal val dim: DefaultIntBuf,
        internal val levels: LinkedList<Dimension>) : Dimension() {

    fun copy() = GeneralDimension(dim.copy(), LinkedList(levels))

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
    constructor(start: Int, val successors: levelsIterator)
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
    }

    override fun sum(slot: MutableIntBuf, successors: levelsIterator): Int {
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

    private inline fun cascadeSum(slot: MutableIntBuf, successors: levelsIterator)
            = if (successors.hasNext()) successors.next().sum(slot, successors) else 1

    override fun <E : Any> CNSet(set: CompactNSet<E>, offset: Int,
                                 slot: MutableIntBuf, successors: levelsIterator,
                                 recipe: (IntBuf) -> E) {
        val successors = levelsItr(0, successors)
        cascadeDFS(set, offset, slot, dim.reuseBacked(), successors, recipe)
    }

    private fun <E : Any> cascadeDFS(set: CompactNSet<E>, offset: Int,
                                     slot: MutableIntBuf, dim: MutableIntBuf, successors: levelsIterator,
                                     recipe: (IntBuf) -> E) {
        if (dim.isEmpty || dim.isZero()) {
            if (successors.hasNext())
                successors.next().CNSet(set, offset, slot, successors, recipe)
            else set._set(offset, recipe(slot))
        } else {
            val d = dim[0]
            dim.removeFirst(1)
            val subtree = set.expand(offset, d)

            slot.append(0)
            cascadeDFS(set, offset, slot, dim, successors.snapshot(), recipe)
            slot[slot.lastIndex]++
            for (a in 1 until d) {
                cascadeDFS(set, subtree.offset2nd + a - 1, slot,
                           dim, successors.snapshot(), recipe)
                slot[slot.lastIndex]++
            }
            slot.removeLast(1)
            dim.prepend(d)
            subtree.offsetEnd = set.data.writePtr - 1
        }
    }
}

