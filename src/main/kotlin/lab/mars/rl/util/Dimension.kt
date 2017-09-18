@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util

/**
 * <p>
 * Created on 2017-09-14.
 * </p>
 *
 * @author wumo
 */
class Dimension(val rootDim: DefaultIntSlice, val sub: Array<Dimension>)

val empty = Array<Dimension>(0) { throw IllegalStateException() }

inline operator fun Int.invoke(vararg s: Any) = make(DefaultIntSlice.of(this), s)

inline operator fun DefaultIntSlice.invoke(vararg s: Any) = make(this, s)

fun Any.toDim() = when (this) {
    is Int -> Dimension(DefaultIntSlice.of(this), empty)
    is DefaultIntSlice -> Dimension(this, empty)
    is Dimension -> this
    else -> throw IllegalArgumentException(this.toString())
}

fun make(dim: DefaultIntSlice, s: Array<out Any>): Dimension {
    val array = Array(s.size) {
        val tmp = s[it]
        when (tmp) {
            is Int -> Dimension(DefaultIntSlice.of(tmp), empty)
            is DefaultIntSlice -> Dimension(tmp, empty)
            is Dimension -> tmp
            else -> throw IllegalArgumentException(tmp.toString())
        }
    }
    return Dimension(dim, array)
}

inline fun Dim(vararg dim: Int) = dim

class D {

    constructor(vararg s: Any)
    constructor(a: DefaultIntSlice)
    constructor(a: Int)

    operator fun invoke(vararg s: Any) {

    }
}

inline fun o(a: DefaultIntSlice, block: TreeDim.() -> Unit): TreeDim {
    val d = TreeDim(a)
    block(d)
    return d
}

inline fun o(a: Int, block: TreeDim.() -> Unit) = o(DefaultIntSlice.of(a), block)

inline fun o(block: TreeDim.() -> Unit) = o(DefaultIntSlice.of(0), block)

inline fun o(a: DefaultIntSlice) = LeafDim(a)

inline fun o(a: Int) = LeafDim(DefaultIntSlice.of(a))

infix fun DefaultIntSlice.x(a: Int) = apply { append(a) }

infix fun Int.x(a: Int) = DefaultIntSlice.of(this, a)

operator fun DefaultIntSlice.invoke(block: TreeDim.() -> Unit): TreeDim {
    val tmp = TreeDim(this)
    block(tmp)
    return tmp
}

operator fun Int.invoke(block: TreeDim.() -> Unit): TreeDim {
    val tmp = TreeDim(DefaultIntSlice.of(this))
    block(tmp)
    return tmp
}

interface IDim
class LeafDim(val idx: DefaultIntSlice) : IDim

class TreeDim(val dim: DefaultIntSlice) : IDim {
    constructor(dim: Int) : this(DefaultIntSlice.of(dim))
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
            special[a] = LeafDim(DefaultIntSlice.of(s))
        }

        operator fun set(vararg a: Int, s: DefaultIntSlice) {
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
            sub.add(LeafDim(DefaultIntSlice.of(a)))
        }

        operator fun invoke(a: DefaultIntSlice) {
            sub.add(LeafDim(a))
        }

        inline operator fun invoke(a: DefaultIntSlice, block: TreeDim.() -> Unit) {
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
