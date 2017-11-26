@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util.math

import lab.mars.rl.util.tuples.tuple2
import java.util.concurrent.ThreadLocalRandom

inline fun Rand() = ThreadLocalRandom.current()!!

fun repeat(times: Int, condition: (Int) -> Boolean, action: (Int) -> Unit) {
    for (index in 0 until times) {
        if (!condition(index)) break
        action(index)
    }
}

fun <T> Π(set: Iterable<T>, evaluate: T.(T) -> Double): Double {
    var multi = 1.0
    set.forEach {
        multi *= it.evaluate(it)
    }
    return multi
}

fun <T> Σ(set: Iterable<T>, evaluate: T.(T) -> Double): Double {
    var sum = 0.0
    set.forEach {
        sum += it.evaluate(it)
    }
    return sum
}

fun <T> max(set: Iterable<T>, default: Double = Double.NaN, evaluate: T.(T) -> Double): Double {
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

fun <T> max_count(set: Iterable<T>, default: Double = Double.NaN, evaluate: T.(T) -> Double): tuple2<Double, Int> {
    val iterator = set.iterator()
    if (!iterator.hasNext()) return tuple2(default, 0)
    var tmp = iterator.next()
    var max = evaluate(tmp, tmp)
    var count = 1
    while (iterator.hasNext()) {
        tmp = iterator.next()
        val p = evaluate(tmp, tmp)
        if (p > max) {
            max = p
            count = 1
        } else if (p == max)
            count++
    }
    return tuple2(max, count)
}

fun <T> argmax(set: Iterable<T>, evaluate: T.(T) -> Double): T {
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

fun <T> argmax_tie_random(set: Iterable<T>, evaluate: T.(T) -> Double): T {
    val iterator = set.iterator()
    val max_a = mutableListOf(iterator.next())
    var max = evaluate(max_a[0], max_a[0])
    while (iterator.hasNext()) {
        val tmp = iterator.next()
        val p = evaluate(tmp, tmp)
        if (p > max) {
            max = p
            max_a.apply {
                clear()
                add(tmp)
            }
        } else if (p == max)
            max_a.add(tmp)
    }
    return max_a[Rand().nextInt(max_a.size)]
}