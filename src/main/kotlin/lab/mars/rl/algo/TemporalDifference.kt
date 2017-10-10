package lab.mars.rl.algo

import lab.mars.rl.model.MDP
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.model.StateValueFunction
import lab.mars.rl.util.emptyNSet

/**
 * <p>
 * Created on 2017-10-09.
 * </p>
 *
 * @author wumo
 */
class TemporalDifference(val mdp: MDP, private var policy: NonDeterminedPolicy = emptyNSet()) {
    val gamma = mdp.gamma
    val states = mdp.states
    var max_iteration: Int = 10000
    var alpha: Double = 0.001
    fun prediction(): StateValueFunction {
        val V = mdp.VFunc { 0.0 }
        for (iteration in 0..max_iteration) {
            println("$iteration/$max_iteration")
            var s = states.rand()
            if (s.actions.isEmpty()) continue

            while (!s.actions.isEmpty()) {
                val a = s.actions.rand(policy(s))
                val possible = a.sample()
                V[s] += alpha * (possible.reward + gamma * V[possible.next] - V[s])
                s = possible.next
            }
        }
        return V
    }
}