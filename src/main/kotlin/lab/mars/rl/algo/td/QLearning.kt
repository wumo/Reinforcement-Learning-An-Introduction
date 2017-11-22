package lab.mars.rl.algo.td

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.algo.td.TemporalDifference.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.debug
import lab.mars.rl.util.max
import lab.mars.rl.util.tuples.tuple3


fun TemporalDifference.QLearning(_alpha: (IndexedState, IndexedAction) -> Double = { _, _ -> α }): OptimalSolution {
    val π = indexedMdp.QFunc { 0.0 }
    val Q = indexedMdp.QFunc { 0.0 }

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        while (s.isNotTerminal()) {
            `ε-greedy`(s, Q, π, ε)
            val a = s.actions.rand(π(s))
            val (s_next, reward) = a.sample()
            Q[s, a] += _alpha(s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
            s = s_next
        }
    }
    val V = indexedMdp.VFunc { 0.0 }
    val result = tuple3(π, V, Q)
    V_from_Q_ND(states, result)
    return result
}