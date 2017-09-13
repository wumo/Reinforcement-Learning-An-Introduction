package lab.mars.rl.algo

import lab.mars.rl.model.*
import org.apache.commons.math3.util.FastMath.*

/**
 * <p>
 * Created on 2017-09-05.
 * </p>
 *
 * @author wumo
 */
class PolicyIteration(mdp: MDP) {
    val states = mdp.states
    private val gamma = mdp.gamma
    private val V = mdp.v_maker()
    private val PI = mdp.pi_maker()
    private val Q = mdp.q_maker()

    fun v_iteration(): StateValueFunction {
        //Initialization
        for (s in states) {
            PI[s!!] = s.actions.firstOrNull()
        }
        do {
            //Policy Evaluation
            do {
                var delta = 0.0
                for (s in states) {
                    val v = V[s!!]
                    PI[s]?.possibles?.apply {
                        V[s] = sigma(this) { probability * (reward + gamma * V[next]) }
                        delta = max(delta, abs(v - V[s]))
                    }
                }
                println("delta=$delta")
            } while (delta >= theta)

            //Policy Improvement
            var policy_stable = true
            for (s in states) {
                val old_action = PI[s!!]
                PI[s] = argmax(s.actions) { sigma(possibles) { probability * (reward + gamma * V[next]) } }
                if (old_action !== PI[s]) policy_stable = false
            }
        } while (!policy_stable)
        return V
    }

    fun q_iteration(): ActionValueFunction {
        //Initialization
        for (s in states) {
            PI[s!!] = s.actions.firstOrNull()
        }
        do {
            //Policy Evaluation
            do {
                var delta = 0.0
                for (s in states) {
                    for (a in s?.actions!!) {
                        val q = Q[s, a]
                        Q[s, a] = sigma(a.possibles) { probability * (reward + gamma * Q[next, PI[next]!!]) }
                        delta = max(delta, abs(q - Q[s, a]))
                    }
                }
                println("delta=$delta")
            } while (delta >= theta)

            //Policy Improvement
            var policy_stable = true
            for (s in states) {
                val old_action = PI[s!!]
                PI[s] = argmax(s.actions) { Q[s, this] }
                if (old_action !== PI[s]) policy_stable = false
            }
        } while (!policy_stable)
        return Q
    }

}