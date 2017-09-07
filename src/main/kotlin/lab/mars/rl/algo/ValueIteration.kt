package lab.mars.rl.algo

import lab.mars.rl.model.MDP
import lab.mars.rl.model.StateValueFunction
import org.apache.commons.math3.util.FastMath.abs
import org.apache.commons.math3.util.FastMath.max

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
class ValueIteration(mdp: MDP) {
    val states = mdp.states
    val gamma = mdp.gamma
    val V = mdp.v_maker()
    val PI = mdp.pi_maker()
    val Q = mdp.q_maker()

    fun iteration(): StateValueFunction {
        //value iteration
        do {
            var delta = 0.0
            for (s in states) {
                val v = V[s!!]
                if (s.actions.any()) {
                    V[s] = max(s.actions) { sigma(possibles) { probability * (reward + gamma * V[next]) } }
                    delta = max(delta, abs(v - V[s]))
                }
            }
            println("delta=$delta")
        } while (delta >= theta)
        //policy generation
        for (s in states) {
            PI[s!!] = argmax(s.actions) { sigma(possibles) { probability * (reward + gamma * V[next]) } }
        }
        return V
    }
}