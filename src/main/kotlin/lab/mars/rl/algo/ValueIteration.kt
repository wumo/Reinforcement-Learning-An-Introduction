package lab.mars.rl.algo

import lab.mars.rl.model.MDP
import lab.mars.rl.model.StateValueFunction
import lab.mars.rl.model.null_action
import lab.mars.rl.util.argmax
import lab.mars.rl.util.max
import lab.mars.rl.util.sigma
import org.apache.commons.math3.util.FastMath.abs
import org.apache.commons.math3.util.FastMath.max

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
class ValueIteration(private val mdp: MDP) {
    val states = mdp.states
    val gamma = mdp.gamma
    fun iteration(): StateValueFunction {
        val V = mdp.VFunc { 0.0 }
        val PI = mdp.VFunc { null_action }
        //value iteration
        do {
            var delta = 0.0
            for (s in states) {
                s.actions.ifAny {
                    val v = V[s]
                    V[s] = max(this) { sigma(possibles) { probability * (reward + gamma * V[next]) } }
                    delta = max(delta, abs(v - V[s]))
                }
            }
            println("delta=$delta")
        } while (delta >= theta)
        //policy generation
        for (s in states)
            s.actions.ifAny {
                PI[s] = argmax(s.actions) { sigma(possibles) { probability * (reward + gamma * V[next]) } }
            }
        return V
    }
}