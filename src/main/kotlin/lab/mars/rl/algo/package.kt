package lab.mars.rl.algo

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
const val theta = 1e-6

inline fun <T> sigma(set: Iterable<T>, evaluate: T.() -> Double): Double {
    var sum = 0.0
    set.forEach {
        sum += it.evaluate()
    }
    return sum
}

inline fun <T> max(set: Iterable<T>, evaluate: T.() -> Double): Double {
    var max = Double.NEGATIVE_INFINITY
    set.forEach {
        val p = evaluate(it)
        if (p > max)
            max = p
    }
    return max
}

inline fun <T> argmax(set: Iterable<T>, evaluate: T.() -> Double): T? {
    var max = Double.NEGATIVE_INFINITY
    var max_a: T? = null
    set.forEach {
        val p = evaluate(it)
        if (p > max) {
            max = p
            max_a = it
        }
    }
    return max_a
}
