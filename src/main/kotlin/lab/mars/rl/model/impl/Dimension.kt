@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.model.impl

import lab.mars.rl.util.IntSlice

/**
 * <p>
 * Created on 2017-09-14.
 * </p>
 *
 * @author wumo
 */
class Dimension(val rootDim: IntSlice, val sub: Array<Dimension>)

val empty = Array<Dimension>(0) { throw IllegalStateException() }

inline operator fun Int.invoke(vararg s: Any) = make(IntSlice.of(this), s)

inline operator fun IntSlice.invoke(vararg s: Any) = make(this, s)

fun make(dim: IntSlice, s: Array<out Any>): Dimension {
    val array = Array(s.size) {
        val tmp = s[it]
        when (tmp) {
            is Int -> Dimension(IntSlice.of(tmp), empty)
            is IntSlice -> Dimension(tmp, empty)
            is Dimension -> tmp
            else -> throw IllegalArgumentException(tmp.toString())
        }
    }
    return Dimension(dim, array)
}

inline fun Dim(vararg dim: Int) = dim

class D {

    constructor(vararg s: Any)
    constructor(a: IntSlice)
    constructor(a: Int)

    operator fun invoke(vararg s: Any) {

    }
}

inline fun o(a: IntSlice, block: TreeDim.() -> Unit): TreeDim {
    val d = TreeDim(a)
    block(d)
    return d
}

inline fun o(a: Int, block: TreeDim.() -> Unit) = o(IntSlice.of(a), block)

inline fun o(block: TreeDim.() -> Unit) = o(IntSlice.of(0), block)

inline fun o(a: IntSlice) = LeafDim(a)

inline fun o(a: Int) = LeafDim(IntSlice.of(a))

infix fun IntSlice.x(a: Int) = apply { append(a) }

infix fun Int.x(a: Int) = IntSlice.of(this, a)

operator fun IntSlice.invoke(block: TreeDim.() -> Unit): TreeDim {
    val tmp = TreeDim(this)
    block(tmp)
    return tmp
}

operator fun Int.invoke(block: TreeDim.() -> Unit): TreeDim {
    val tmp = TreeDim(IntSlice.of(this))
    block(tmp)
    return tmp
}

interface IDim
class LeafDim(val idx: IntSlice) : IDim

class TreeDim(val dim: IntSlice) : IDim {
    constructor(dim: Int) : this(IntSlice.of(dim))
    constructor() : this(0)

    val sub = arrayListOf<IDim>()
    val special = hashMapOf<IntArray, IDim>()
    val excludes = hashSetOf<IntArray>()

    inner class exclude internal constructor()

    val x = exclude()

    inner class DimDefinition {
        operator fun set(vararg a: Int, s: exclude) {
            excludes.add(a)
        }

        operator fun set(vararg a: Int, s: Int) {
            special[a] = LeafDim(IntSlice.of(s))
        }

        operator fun set(vararg a: Int, s: IntSlice) {
            special[a] = LeafDim(s)
        }

        operator fun set(vararg a: Int, s: TreeDim) {
            special[a] = s
        }

        operator fun set(vararg a: Int, block: TreeDim.() -> Unit) {
            val s = TreeDim()
            block(s)
            special[a] = s
        }

        operator fun invoke(a: Int) {
            sub.add(LeafDim(IntSlice.of(a)))
        }

        operator fun invoke(a: IntSlice) {
            sub.add(LeafDim(a))
        }

        inline operator fun invoke(a: IntSlice, block: TreeDim.() -> Unit) {
            val s = TreeDim(a)
            block(s)
            sub.add(s)
        }

        inline operator fun invoke(block: TreeDim.() -> Unit) {
            val s = TreeDim()
            block(s)
            sub.add(s)
        }
    }

    val o = DimDefinition()
    fun except(block: TreeDim.() -> Unit) {
        block(this)
    }

}
