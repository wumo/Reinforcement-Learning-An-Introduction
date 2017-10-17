package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.*
import org.slf4j.LoggerFactory

/**
 * <p>
 * Created on 2017-10-09.
 * </p>
 *
 * @author wumo
 */
class TemporalDifference(val mdp: MDP, private var policy: NonDeterminedPolicy = emptyNSet()) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val gamma = mdp.gamma
    val started = mdp.started
    val states = mdp.states
    var episodes = 10000
    var alpha = 0.1
    var epsilon = 0.1

    fun prediction(): StateValueFunction {
        val V = mdp.VFunc { 0.0 }
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started.rand()
            while (s.isNotTerminal()) {
                val a = s.actions.rand(policy(s))
                val possible = a.sample()
                V[s] += alpha * (possible.reward + gamma * V[possible.next] - V[s])
                s = possible.next
            }
        }
        return V
    }

    fun sarsa(): OptimalSolution {
        val policy = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { 0.0 }

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started.rand()
            `e-greedy`(s, Q, policy)
            var a = s.actions.rand(policy(s))
            while (true) {
                val possible = a.sample()
                val s_next = possible.next
                if (s_next.isNotTerminal()) {
                    `e-greedy`(s_next, Q, policy)
                    val a_next = s_next.actions.rand(policy(s_next))
                    Q[s, a] += alpha * (possible.reward + gamma * Q[s_next, a_next] - Q[s, a])
                    s = s_next
                    a = a_next
                } else {
                    Q[s, a] += alpha * (possible.reward + gamma * 0.0 - Q[s, a])//Q[terminalState,*]=0.0
                    break
                }
            }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(policy, V, Q)
        V_from_Q_ND(states, result)
        return result
    }

    private fun `e-greedy`(s: State, Q: ActionValueFunction, policy: NonDeterminedPolicy) {
        val `a*` = argmax(s.actions) { Q[s, it] }
        val size = s.actions.size
        for (a in s.actions) {
            policy[s, a] = when {
                a === `a*` -> 1 - epsilon + epsilon / size
                else -> epsilon / size
            }
        }
    }

    fun QLearning(): OptimalSolution {
        val policy = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { 0.0 }

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started.rand()
            while (true) {
                `e-greedy`(s, Q, policy)
                val a = s.actions.rand(policy(s))
                val possible = a.sample()
                val s_next = possible.next
                if (s_next.isNotTerminal()) {
                    Q[s, a] += alpha * (possible.reward + gamma * max(s_next.actions) { Q[s_next, it] } - Q[s, a])
                    s = s_next
                } else {
                    Q[s, a] += alpha * (possible.reward + gamma * 0.0 - Q[s, a])//Q[terminalState,*]=0.0
                    break
                }
            }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(policy, V, Q)
        V_from_Q_ND(states, result)
        return result
    }

    fun expectedSarsa(): OptimalSolution {
        val policy = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { 0.0 }

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started.rand()
            while (true) {
                `e-greedy`(s, Q, policy)
                val a = s.actions.rand(policy(s))
                val possible = a.sample()
                val s_next = possible.next
                if (s_next.isNotTerminal()) {
                    Q[s, a] += alpha * (possible.reward + gamma * Sigma(s_next.actions) { policy[s_next, it] * Q[s_next, it] } - Q[s, a])
                    s = s_next
                } else {
                    Q[s, a] += alpha * (possible.reward + gamma * 0.0 - Q[s, a])//Q[terminalState,*]=0.0
                    break
                }
            }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(policy, V, Q)
        V_from_Q_ND(states, result)
        return result
    }

    fun DoubleQLearning(): OptimalSolution {
        val policy = mdp.QFunc { 0.0 }
        var Q1 = mdp.QFunc { 0.0 }
        var Q2 = mdp.QFunc { 0.0 }

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started.rand()
            while (true) {
                `e-greedy`(s, Q1, Q2, policy)
                val a = s.actions.rand(policy(s))
                val possible = a.sample()
                val s_next = possible.next
                if (Rand().nextBoolean()) {
                    val tmp = Q1
                    Q1 = Q2
                    Q2 = tmp
                }
                if (s_next.isNotTerminal()) {
                    Q1[s, a] += alpha * (possible.reward + gamma * Q2[s_next, argmax(s_next.actions) { Q1[s_next, it] }] - Q1[s, a])
                    s = s_next
                } else {
                    Q1[s, a] += alpha * (possible.reward + gamma * 0.0 - Q1[s, a])//Q[terminalState,*]=0.0
                    break
                }
            }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(policy, V, Q1)
        V_from_Q_ND(states, result)
        return result
    }

    private fun `e-greedy`(s: State, Q1: ActionValueFunction, Q2: ActionValueFunction, policy: NonDeterminedPolicy) {
        val `a*` = argmax(s.actions) { Q1[s, it] + Q2[s, it] }
        val size = s.actions.size
        for (a in s.actions) {
            policy[s, a] = when {
                a === `a*` -> 1 - epsilon + epsilon / size
                else -> epsilon / size
            }
        }
    }
}