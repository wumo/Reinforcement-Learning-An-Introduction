@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util

import java.util.concurrent.ThreadLocalRandom

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
const val theta = 1e-6

inline fun Rand() = ThreadLocalRandom.current()

data class tuple2<A, B>(var _1: A, var _2: B) {
    override fun toString(): String {
        return "$_1=$_2"
    }
}

data class tuple3<A, B, C>(var _1: A, var _2: B, var _3: C) {
    override fun toString(): String {
        return "($_1,$_2,$_3)"
    }
}

data class tuple4<A, B, C, D>(var _1: A, var _2: B, var _3: C, var _4: D) {
    override fun toString(): String {
        return "($_1,$_2,$_3,$_4)"
    }
}
inline fun Pi(from: Int, to: Int, evaluate: (Int) -> Double): Double {
    var multi = 1.0
    for (a in from..to)
        multi *= evaluate(a)
    return multi
}

inline fun <T> Sigma(set: Iterable<T>, evaluate: T.(T) -> Double): Double {
    var sum = 0.0
    set.forEach {
        sum += it.evaluate(it)
    }
    return sum
}

inline fun Sigma(from: Int, to: Int, evaluate: (Int) -> Double): Double {
    var sum = 0.0
    for (a in from..to)
        sum += evaluate(a)
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