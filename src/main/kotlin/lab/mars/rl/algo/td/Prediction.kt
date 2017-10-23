package lab.mars.rl.algo.td

import lab.mars.rl.algo.td.TemporalDifference.Companion.log
import lab.mars.rl.model.StateValueFunction
import lab.mars.rl.util.debug

fun TemporalDifference.prediction(): StateValueFunction {
    val V = mdp.VFunc { 0.0 }
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        while (s.isNotTerminal()) {
            val a = s.actions.rand(initial_policy(s))
            val possible = a.sample()
            V[s] += alpha * (possible.reward + gamma * V[possible.next] - V[s])
            s = possible.next
        }
    }
    return V
}