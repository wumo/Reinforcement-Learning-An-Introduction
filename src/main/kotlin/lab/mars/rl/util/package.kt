package lab.mars.rl.util

import lab.mars.rl.model.*
import java.util.concurrent.ThreadLocalRandom

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
const val theta = 1e-6

inline fun Rand()= ThreadLocalRandom.current()
inline fun <T> sigma(set: Iterable<T>, evaluate: T.() -> Double): Double {
    var sum = 0.0
    set.forEach {
        sum += it.evaluate()
    }
    return sum
}

inline fun <T> max(set: Iterable<T>, evaluate: T.() -> Double): Double {
    val iterator = set.iterator()
    var max = evaluate(iterator.next())
    while (iterator.hasNext()) {
        val p = evaluate(iterator.next())
        if (p > max)
            max = p
    }
    return max
}

inline fun <T> argmax(set: Iterable<T>, evaluate: T.() -> Double): T {
    val iterator = set.iterator()
    var max_a: T = iterator.next()
    var max = evaluate(max_a)
    while (iterator.hasNext()) {
        val tmp = iterator.next()
        val p = evaluate(tmp)
        if (p > max) {
            max = p
            max_a = tmp
        }
    }
    return max_a
}