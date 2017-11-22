package lab.mars.rl.algo.td

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.algo.td.TemporalDifference.Companion.log
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.tuples.tuple3

fun TemporalDifference.sarsa(_alpha: (IndexedState, IndexedAction) -> Double = { _, _ -> α }): OptimalSolution {
    val π = IndexedPolicy(indexedMdp.QFunc { 0.0 })
    val Q = indexedMdp.QFunc { 0.0 }

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        `ε-greedy`(s, Q, π, ε)
        var a = π(s)
        while (true) {
            val (s_next, reward) = a.sample()
            if (s_next.isNotTerminal()) {
                `ε-greedy`(s_next, Q, π, ε)
                val a_next = π(s_next)
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
    V_from_Q(states, result)
    return result
}