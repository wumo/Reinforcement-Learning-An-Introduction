package lab.mars.rl.algo.td

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.td.TemporalDifference.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.Rand
import lab.mars.rl.util.argmax
import lab.mars.rl.util.debug
import lab.mars.rl.util.tuples.tuple3


fun TemporalDifference.DoubleQLearning(_alpha: (State, Action) -> Double = { _, _ -> α }): OptimalSolution {
    val π = mdp.QFunc { 0.0 }
    var Q1 = mdp.QFunc { 0.0 }
    var Q2 = mdp.QFunc { 0.0 }

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        while (true) {
            `ε-greedy`(s, Q1, Q2, π)
            val a = s.actions.rand(π(s))
            val (s_next, reward, _) = a.sample()
            if (Rand().nextBoolean()) {
                val tmp = Q1
                Q1 = Q2
                Q2 = tmp
            }
            if (s_next.isNotTerminal()) {
                Q1[s, a] += _alpha(s, a) * (reward + γ * Q2[s_next, argmax(s_next.actions) { Q1[s_next, it] }] - Q1[s, a])
                s = s_next
            } else {
                Q1[s, a] += _alpha(s, a) * (reward + γ * 0.0 - Q1[s, a])//Q[terminalState,*]=0.0
                break
            }
        }
    }
    val V = mdp.VFunc { 0.0 }
    val result = tuple3(π, V, Q1)
    V_from_Q_ND(states, result)
    return result
}

private fun TemporalDifference.`ε-greedy`(s: State, Q1: ActionValueFunction, Q2: ActionValueFunction, π: NonDeterminedPolicy) {
    val `a*` = argmax(s.actions) { Q1[s, it] + Q2[s, it] }
    val size = s.actions.size
    for (a in s.actions) {
        π[s, a] = when {
            a === `a*` -> 1 - ε + ε / size
            else -> ε / size
        }
    }
}