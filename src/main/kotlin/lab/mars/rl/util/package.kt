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

data class tuple2<A, B>(var first: A, var second: B) {
    override fun toString(): String {
        return "$first=$second"
    }
}

data class tuple4<A, B, C, D>(var _1: A, var _2: B, var _3: C, var _4: D) {
    override fun toString(): String {
        return "($_1,$_2,$_3,$_3)"
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

inline fun <T> max(set: Iterable<T>, evaluate: T.(T) -> Double): Double {
    val iterator = set.iterator()
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