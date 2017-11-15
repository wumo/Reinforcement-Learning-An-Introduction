package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.argmax
import lab.mars.rl.util.argmax_tie_random
import lab.mars.rl.util.`Σ`
import lab.mars.rl.util.tuples.tuple3

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
const val `θ` = 1e-6

fun V_from_Q(states: StateSet, pvq: tuple3<DeterminedPolicy, StateValueFunction, ActionValueFunction>) {
    val (PI, V, Q) = pvq
    for (s in states)
        s.actions.ifAny {
            V[s] = Q[s, PI[s]]
        }
}

fun V_from_Q_ND(states: StateSet, pvq: tuple3<NonDeterminedPolicy, StateValueFunction, ActionValueFunction>) {
    val (PI, V, Q) = pvq
    for (s in states)
        s.actions.ifAny {
            var sum = 0.0
            for ((a, prob) in PI(s).withIndices())
                sum += prob * Q[s, a]
            V[s] = sum
        }
}

fun Q_from_V(gamma: Double, states: StateSet, pvq: tuple3<DeterminedPolicy, StateValueFunction, ActionValueFunction>) {
    val (_, V, Q) = pvq
    for (s in states)
        for (a in s.actions)
            Q[s, a] = `Σ`(a.possibles) { probability * (reward + gamma * V[next]) }
}

fun average_alpha(mdp: MDP): (State, Action) -> Double {
    val N = mdp.QFunc { 0 }
    return { s, a ->
        N[s, a]++
        1.0 / N[s, a]
    }
}

fun `ε-greedy`(s: State, Q: ActionValueFunction, policy: NonDeterminedPolicy, `ε`: Double) {
    val `a*` = argmax(s.actions) { Q[s, it] }
    val size = s.actions.size
    for (a in s.actions) {
        policy[s, a] = when {
            a === `a*` -> 1 - `ε` + `ε` / size
            else -> `ε` / size
        }
    }
}

fun `ε-greedy tie random`(s: State, Q: ActionValueFunction, policy: NonDeterminedPolicy, `ε`: Double) {
    val `a*` = argmax_tie_random(s.actions) { Q[s, it] }
    val size = s.actions.size
    for (a in s.actions) {
        policy[s, a] = when {
            a === `a*` -> 1 - `ε` + `ε` / size
            else -> `ε` / size
        }
    }
}