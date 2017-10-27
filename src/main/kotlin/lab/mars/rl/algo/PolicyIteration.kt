package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.argmax
import lab.mars.rl.util.debug
import lab.mars.rl.util.Sigma
import lab.mars.rl.util.tuple3
import org.apache.commons.math3.util.FastMath.abs
import org.apache.commons.math3.util.FastMath.max
import org.slf4j.LoggerFactory

/**
 * <p>
 * Created on 2017-09-05.
 * </p>
 *
 * @author wumo
 */
class PolicyIteration(mdp: MDP) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val states = mdp.states
    private val gamma = mdp.gamma
    private val V = mdp.VFunc { 0.0 }
    private val PI = mdp.VFunc { null_action }
    private val Q = mdp.QFunc { 0.0 }

    fun v_iteration(): tuple3<DeterminedPolicy, StateValueFunction, ActionValueFunction> {
        //Initialization
        for (s in states)
            PI[s] = s.actions.firstOrNull() ?: null_action

        do {
            //Policy Evaluation
            do {
                var delta = 0.0
                for (s in states)
                    s.actions.ifAny {
                        val v = V[s]
                        V[s] = Sigma(PI[s].possibles) { probability * (reward + gamma * V[next]) }
                        delta = max(delta, abs(v - V[s]))
                    }
                log.debug { "delta=$delta" }
            } while (delta >= theta)

            //Policy Improvement
            var policy_stable = true
            for (s in states)
                s.actions.ifAny {
                    val old_action = PI[s]
                    PI[s] = argmax(s.actions) { Sigma(possibles) { probability * (reward + gamma * V[next]) } }
                    if (old_action !== PI[s]) policy_stable = false
                }
        } while (!policy_stable)
        val result = tuple3(PI, V, Q)
        Q_from_V(gamma, states, result)
        return result
    }

    fun q_iteration(): tuple3<DeterminedPolicy, StateValueFunction, ActionValueFunction> {
        //Initialization
        for (s in states)
            PI[s] = s.actions.firstOrNull() ?: null_action

        do {
            //Policy Evaluation
            do {
                var delta = 0.0
                for (s in states) {
                    for (a in s.actions) {
                        val q = Q[s, a]
                        Q[s, a] = Sigma(a.possibles) { probability * (reward + gamma * if (next.actions.any()) Q[next, PI[next]] else 0.0) }
                        delta = max(delta, abs(q - Q[s, a]))
                    }
                }
                log.debug { "delta=$delta" }
            } while (delta >= theta)

            //Policy Improvement
            var policy_stable = true
            for (s in states)
                s.actions.ifAny {
                    val old_action = PI[s]
                    PI[s] = argmax(s.actions) { Q[s, it] }
                    if (old_action !== PI[s]) policy_stable = false
                }
        } while (!policy_stable)
        val result = tuple3(PI, V, Q)
        V_from_Q(states, result)
        return result
    }

}