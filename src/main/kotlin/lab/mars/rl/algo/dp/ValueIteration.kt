package lab.mars.rl.algo.dp

import lab.mars.rl.algo.θ
import lab.mars.rl.model.MDP
import lab.mars.rl.model.StateValueFunction
import lab.mars.rl.model.null_action
import lab.mars.rl.util.argmax
import lab.mars.rl.util.debug
import lab.mars.rl.util.max
import lab.mars.rl.util.Σ
import org.apache.commons.math3.util.FastMath.abs
import org.apache.commons.math3.util.FastMath.max
import org.slf4j.LoggerFactory

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
class ValueIteration(private val mdp: MDP) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val states = mdp.states
    val γ = mdp.γ
    fun iteration(): StateValueFunction {
        val V = mdp.VFunc { 0.0 }
        val PI = mdp.VFunc { null_action }
        //value iteration
        do {
            var Δ = 0.0
            for (s in states) {
                s.actions.ifAny {
                    val v = V[s]
                    V[s] = max(it) { Σ(possibles) { probability * (reward + γ * V[next]) } }
                    Δ = max(Δ, abs(v - V[s]))
                }
            }
            log.debug { "Δ=$Δ" }
        } while (Δ >= θ)
        //policy generation
        for (s in states)
            s.actions.ifAny {
                PI[s] = argmax(s.actions) { Σ(possibles) { probability * (reward + γ * V[next]) } }
            }
        return V
    }
}