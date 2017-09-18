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

val emptyDim = Array<Dimension>(0) { throw IllegalStateException() }

inline operator fun Int.invoke(vararg s: Any) = make(DefaultIntSlice.of(this), s)
inline infix fun Int.x(a: Int) = DefaultIntSlice.of(this, a)

inline operator fun DefaultIntSlice.invoke(vararg s: Any) = make(this, s)
inline infix fun DefaultIntSlice.x(a: Int) = apply { append(a) }

fun Any.toDim() = when (this) {
    is Int -> Dimension(DefaultIntSlice.of(this), emptyDim)
    is DefaultIntSlice -> Dimension(this, emptyDim)
    is Dimension -> this
    else -> throw IllegalArgumentException(this.toString())
}

fun make(dim: DefaultIntSlice, s: Array<out Any>): Dimension {
    return Dimension(dim, Array(s.size) {
        val tmp = s[it]
        when (tmp) {
            is Int -> Dimension(DefaultIntSlice.of(tmp), emptyDim)
            is DefaultIntSlice -> Dimension(tmp, emptyDim)
            is Dimension -> tmp
            else -> throw IllegalArgumentException(tmp.toString())
        }
    })
}