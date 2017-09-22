package lab.mars.rl.algo

import lab.mars.rl.model.ActionValueFunction
import lab.mars.rl.model.DeterminedPolicy
import lab.mars.rl.model.StateSet
import lab.mars.rl.model.StateValueFunction

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


fun V_from_Q(states: StateSet, pvq: Triple<DeterminedPolicy, StateValueFunction, ActionValueFunction>) {
    val (PI, V, Q) = pvq
    for (s in states)
        s.actions.ifAny {
            V[s] = Q[s, PI[s]]
        }
}

fun Q_from_V(gamma: Double, states: StateSet, pvq: Triple<DeterminedPolicy, StateValueFunction, ActionValueFunction>) {
    val (_, V, Q) = pvq
    for (s in states)
        for (a in s.actions)
            Q[s, a] = sigma(a.possibles) { probability * (reward + gamma * V[next]) }
}