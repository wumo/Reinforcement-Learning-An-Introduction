package lab.mars.rl.algo.dp

import lab.mars.rl.algo.Q_from_V
import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.θ
import lab.mars.rl.model.*
import lab.mars.rl.util.argmax
import lab.mars.rl.util.debug
import lab.mars.rl.util.tuples.tuple3
import lab.mars.rl.util.Σ
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
    private val `γ` = mdp.`γ`
    private val V = mdp.VFunc { 0.0 }
    private val `π` = mdp.VFunc { null_action }
    private val Q = mdp.QFunc { 0.0 }

    fun v_iteration(): tuple3<DeterminedPolicy, StateValueFunction, ActionValueFunction> {
        //Initialization
        for (s in states)
            `π`[s] = s.actions.firstOrNull() ?: null_action

        do {
            //Policy Evaluation
            do {
                var `Δ` = 0.0
                for (s in states)
                    s.actions.ifAny {
                        val v = V[s]
                        V[s] = `Σ`(`π`[s].possibles) { probability * (reward + `γ` * V[next]) }
                        `Δ` = max(`Δ`, abs(v - V[s]))
                    }
                log.debug { "Δ=$`Δ`" }
            } while (`Δ` >= `θ`)

            //Policy Improvement
            var `policy-stable` = true
            for (s in states)
                s.actions.ifAny {
                    val `old-action` = `π`[s]
                    `π`[s] = argmax(s.actions) { `Σ`(possibles) { probability * (reward + `γ` * V[next]) } }
                    if (`old-action` !== `π`[s]) `policy-stable` = false
                }
        } while (!`policy-stable`)
        val result = tuple3(`π`, V, Q)
        Q_from_V(`γ`, states, result)
        return result
    }

    fun q_iteration(): tuple3<DeterminedPolicy, StateValueFunction, ActionValueFunction> {
        //Initialization
        for (s in states)
            `π`[s] = s.actions.firstOrNull() ?: null_action

        do {
            //Policy Evaluation
            do {
                var delta = 0.0
                for (s in states) {
                    for (a in s.actions) {
                        val q = Q[s, a]
                        Q[s, a] = `Σ`(a.possibles) { probability * (reward + `γ` * if (next.actions.any()) Q[next, `π`[next]] else 0.0) }
                        delta = max(delta, abs(q - Q[s, a]))
                    }
                }
                log.debug { "delta=$delta" }
            } while (delta >= `θ`)

            //Policy Improvement
            var policy_stable = true
            for (s in states)
                s.actions.ifAny {
                    val old_action = `π`[s]
                    `π`[s] = argmax(s.actions) { Q[s, it] }
                    if (old_action !== `π`[s]) policy_stable = false
                }
        } while (!policy_stable)
        val result = tuple3(`π`, V, Q)
        V_from_Q(states, result)
        return result
    }

}