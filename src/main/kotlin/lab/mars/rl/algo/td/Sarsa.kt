package lab.mars.rl.algo.td

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.algo.td.TemporalDifference.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.debug
import lab.mars.rl.util.tuples.tuple3

fun TemporalDifference.sarsa(_alpha: (IndexedState, IndexedAction) -> Double = { _, _ -> α }): OptimalSolution {
    val π = indexedMdp.QFunc { 0.0 }
    val Q = indexedMdp.QFunc { 0.0 }

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        `ε-greedy`(s, Q, π, ε)
        var a = s.actions.rand(π(s))
        while (true) {
            val (s_next, reward) = a.sample()
            if (s_next.isNotTerminal()) {
                `ε-greedy`(s_next, Q, π, ε)
                val a_next = s_next.actions.rand(π(s_next))
                Q[s, a] += _alpha(s, a) * (reward + γ * Q[s_next, a_next] - Q[s, a])
                s = s_next
                a = a_next
            } else {
                Q[s, a] += _alpha(s, a) * (reward + γ * 0.0 - Q[s, a])//Q[terminalState,*]=0.0
                break
            }
        }
    }
    val V = indexedMdp.VFunc { 0.0 }
    val result = tuple3(π, V, Q)
    V_from_Q_ND(states, result)
    return result
}