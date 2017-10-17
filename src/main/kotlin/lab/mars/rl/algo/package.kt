package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.Sigma

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
const val theta = 1e-6

fun V_from_Q(states: StateSet, pvq: Triple<DeterminedPolicy, StateValueFunction, ActionValueFunction>) {
    val (PI, V, Q) = pvq
    for (s in states)
        s.actions.ifAny {
            V[s] = Q[s, PI[s]]
        }
}

fun V_from_Q_ND(states: StateSet, pvq: Triple<NonDeterminedPolicy, StateValueFunction, ActionValueFunction>) {
    val (PI, V, Q) = pvq
    for (s in states)
        s.actions.ifAny {
            var sum = 0.0
            for ((a, prob) in PI(s).withIndices())
                sum += prob * Q[s, a]
            V[s] = sum
        }
}

fun Q_from_V(gamma: Double, states: StateSet, pvq: Triple<DeterminedPolicy, StateValueFunction, ActionValueFunction>) {
    val (_, V, Q) = pvq
    for (s in states)
        for (a in s.actions)
            Q[s, a] = Sigma(a.possibles) { probability * (reward + gamma * V[next]) }
}