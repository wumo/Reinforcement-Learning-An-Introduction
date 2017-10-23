package lab.mars.rl.algo.td

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`e-greedy`
import lab.mars.rl.algo.td.TemporalDifference.Companion.log
import lab.mars.rl.model.Action
import lab.mars.rl.model.OptimalSolution
import lab.mars.rl.model.State
import lab.mars.rl.util.debug
import lab.mars.rl.util.max


fun TemporalDifference.QLearning(_alpha: (State, Action) -> Double = { _, _ -> alpha }): OptimalSolution {
    val policy = mdp.QFunc { 0.0 }
    val Q = mdp.QFunc { 0.0 }

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        while (true) {
            `e-greedy`(s, Q, policy,epsilon)
            val a = s.actions.rand(policy(s))
            val possible = a.sample()
            val s_next = possible.next
            if (s_next.isNotTerminal()) {
                Q[s, a] += _alpha(s, a) * (possible.reward + gamma * max(s_next.actions) { Q[s_next, it] } - Q[s, a])
                s = s_next
            } else {
                Q[s, a] += _alpha(s, a) * (possible.reward + gamma * 0.0 - Q[s, a])//Q[terminalState,*]=0.0
                break
            }
        }
    }
    val V = mdp.VFunc { 0.0 }
    val result = Triple(policy, V, Q)
    V_from_Q_ND(states, result)
    return result
}