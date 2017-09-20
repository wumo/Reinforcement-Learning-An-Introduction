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
    private val V = mdp.stateFunc<Double> { 0.0 }
    private val PI = mdp.stateFunc<Action> { null_action }
    private val Q = mdp.stateActionFunc<Double> { 0.0 }

    fun v_iteration(): Triple<DeterminedPolicy, StateValueFunction, ActionValueFunction> {
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
                        V[s] = sigma(PI[s].possibles) { probability * (reward + gamma * V[next]) }
                        delta = max(delta, abs(v - V[s]))
                    }
                println("delta=$delta")
            } while (delta >= theta)

            //Policy Improvement
            var policy_stable = true
            for (s in states)
                s.actions.ifAny {
                    val old_action = PI[s]
                    PI[s] = argmax(s.actions) { sigma(possibles) { probability * (reward + gamma * V[next]) } }
                    if (old_action !== PI[s]) policy_stable = false
                }
        } while (!policy_stable)
        V_from_Q(gamma, states, V, Q)
        return Triple(PI, V, Q)
    }

    fun q_iteration(): Triple<DeterminedPolicy, StateValueFunction, ActionValueFunction> {
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
                        Q[s, a] = sigma(a.possibles) { probability * (reward + gamma * if (next.actions.any()) Q[next, PI[next]] else 0.0) }
                        delta = max(delta, abs(q - Q[s, a]))
                    }
                }
                println("delta=$delta")
            } while (delta >= theta)

            //Policy Improvement
            var policy_stable = true
            for (s in states)
                s.actions.ifAny {
                    val old_action = PI[s]
                    PI[s] = argmax(s.actions) { Q[s, this] }
                    if (old_action !== PI[s]) policy_stable = false
                }
        } while (!policy_stable)
        Q_from_V(states, V, Q, PI)
        return Triple(PI, V, Q)
    }

}