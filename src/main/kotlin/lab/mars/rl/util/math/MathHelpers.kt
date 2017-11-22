@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util.math

import java.util.concurrent.ThreadLocalRandom

inline fun Rand() = ThreadLocalRandom.current()!!

inline fun repeat(times: Int, condition: (Int) -> Boolean, action: (Int) -> Unit) {
    for (index in 0 until times) {
        if (!condition(index)) break
        action(index)
    }
}

inline fun <T> Π(set: Iterable<T>, evaluate: T.(T) -> Double): Double {
    var multi = 1.0
    set.forEach {
        multi *= it.evaluate(it)
    }
    return multi
}

inline fun <T> Σ(set: Iterable<T>, evaluate: T.(T) -> Double): Double {
    var sum = 0.0
    set.forEach {
        sum += it.evaluate(it)
    }
    return sum
}

inline fun <T> max(set: Iterable<T>, default: Double = Double.NaN, evaluate: T.(T) -> Double): Double {
    val iterator = set.iterator()
    if (!iterator.hasNext()) return default
    var tmp = iterator.next()
    var max = evaluate(tmp, tmp)
    while (iterator.hasNext()) {
        tmp = iterator.next()
        val p = evaluate(tmp, tmp)
        if (p > max)
            max = p
    }
    return max
}

inline fun <T> argmax(set: Iterable<T>, evaluate: T.(T) -> Double): T {
    val iterator = set.iterator()
    var max_a: T = iterator.next()
    var max = evaluate(max_a, max_a)
    while (iterator.hasNext()) {
        val tmp = iterator.next()
        val p = evaluate(tmp, tmp)
        if (p > max) {
            max = p
            max_a = tmp
        }
    }
    return max_a
}

inline fun <T> argmax_tie_random(set: Iterable<T>, evaluate: T.(T) -> Double): T {
    val iterator = set.iterator()
    var max_a: T = iterator.next()
    var max = evaluate(max_a, max_a)
    while (iterator.hasNext()) {
        val tmp = iterator.next()
        val p = evaluate(tmp, tmp)
        if (p > max) {
            max = p
            max_a = tmp
        } else if (p == max && Rand().nextBoolean())
            max_a = tmp
    }
    return max_a
}