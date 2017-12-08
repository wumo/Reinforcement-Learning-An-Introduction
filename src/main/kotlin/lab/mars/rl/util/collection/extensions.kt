@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package lab.mars.rl.util.collection

import kotlin.sequences.flatMap

inline fun <E, F> Iterable<E>.fork(crossinline subset: (E) -> Iterable<F>)
    = asSequence().flatMap { s -> subset(s).asSequence().map { s to it } }

inline fun <E, F> Sequence<E>.fork(crossinline subset: (E) -> Iterable<F>)
    = flatMap { s -> subset(s).asSequence().map { s to it } }

inline fun <E> Iterable<E>.filter(crossinline predicate: (E) -> Boolean)
    = asSequence().filter { predicate(it) }

inline fun <T, R> Iterable<T>.map(crossinline transform: (T) -> R)
    = asSequence().map { transform(it) }