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
const val theta = 1e-6

class PolicyIteration(mdp: MDP) {
    val states = mdp.states
    val gamma = mdp.gamma
    val V = mdp.v_maker()
    val PI = mdp.pi_maker()
    val Q = mdp.q_maker()

    fun v_iteration() {
        `Initialization`()
        do {
            `Policy Evaluation`()
            val stable = `Policy Improvement`()
        } while (!stable)
    }

    private fun `Initialization`() {
        for (s in states) {
            if (s == null) break
            PI[s] = s.actions?.first()
        }
    }

    private fun `Policy Evaluation`() {
        do {
            var delta = 0.0
            for (s in states) {
                if (s == null) break
                val v = V[s]
                V[s] = sigma(PI[s]?.possibles!!) { probability * (reward + gamma * V[state!!]) }
                delta = max(delta, abs(v - V[s]))
            }
        } while (delta >= theta)
    }

    private fun `Policy Improvement`(): Boolean {
        var policy_stable = true
        for (s in states) {
            if (s == null) break
            val old_action = PI[s]
            PI[s] = argmax(s.actions!!) { sigma(possibles!!) { probability + (reward + gamma * V[state!!]) } }
            if (old_action !== PI[s]) policy_stable = false
        }
        return policy_stable
    }

    private inline fun <T> sigma(set: Iterable<T>, calc: T.() -> Double): Double {
        var sum = 0.0
        set.forEach {
            sum += it.calc()
        }
        return sum
    }

    private inline fun <T> argmax(set: Iterable<T>, evaluate: T.() -> Double): T {
        var max = Double.NEGATIVE_INFINITY
        var max_a: T? = null
        set.forEach {
            val p = evaluate(it)
            if (p > max) {
                max = p
                max_a = it
            }
        }
        return max_a!!
    }
}