package lab.mars.rl.algo.td

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`e-greedy`
import lab.mars.rl.algo.td.TemporalDifference.Companion.log
import lab.mars.rl.model.Action
import lab.mars.rl.model.OptimalSolution
import lab.mars.rl.model.State
import lab.mars.rl.util.debug
import lab.mars.rl.util.tuple3

fun TemporalDifference.sarsa(_alpha: (State, Action) -> Double = { _, _ -> alpha }): OptimalSolution {
    val policy = mdp.QFunc { 0.0 }
    val Q = mdp.QFunc { 0.0 }

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        `e-greedy`(s, Q, policy, epsilon)
        var a = s.actions.rand(policy(s))
        while (true) {
            val (s_next, reward, _) = a.sample()
            if (s_next.isNotTerminal()) {
                `e-greedy`(s_next, Q, policy, epsilon)
                val a_next = s_next.actions.rand(policy(s_next))
                Q[s, a] += _alpha(s, a) * (reward + gamma * Q[s_next, a_next] - Q[s, a])
                s = s_next
                a = a_next
            } else {
                Q[s, a] += _alpha(s, a) * (reward + gamma * 0.0 - Q[s, a])//Q[terminalState,*]=0.0
                break
            }
        }
    }
    val V = mdp.VFunc { 0.0 }
    val result = tuple3(policy, V, Q)
    V_from_Q_ND(states, result)
    return result
}