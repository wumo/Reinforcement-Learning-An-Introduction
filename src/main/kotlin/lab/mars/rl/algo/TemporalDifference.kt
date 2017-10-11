package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.Rand
import lab.mars.rl.util.RandomAccessCollection
import lab.mars.rl.util.argmax
import lab.mars.rl.util.buf.DefaultBuf
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
    var episodes = 10000
    var alpha = 0.1
    var epsilon = 0.1

    fun prediction(): StateValueFunction {
        val V = mdp.VFunc { 0.0 }
        for (episode in 1..episodes) {
            println("$episode/$episodes")
            var s = states.rand()
            if (s.isTerminal()) continue

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
//            println("$episode/$episodes")
            var s = states.rand()
            if (s.isTerminal()) continue
            updatePolicy(s, Q, policy)
            var a = s.actions.rand(policy(s))
            while (true) {
                 val possible = a.sample()
                val s_next = possible.next
                if (s_next.isNotTerminal()) {
                    updatePolicy(s_next, Q, policy)
                    val a_next = s_next.actions.rand(policy(s_next))
                    Q[s, a] += alpha * (possible.reward + gamma * Q[s_next, a_next] - Q[s, a])
                    s = s_next
                    a = a_next
                } else {
                    Q[s, a] += alpha * (possible.reward + gamma * 0.0 - Q[s, a])//Q[terminalState,*]=0.0
                    break
                }
            }
//            for (s in states) {
//                if(s.isTerminal())continue
//                val `a*` = argmax(s.actions) {
//                    Q[s, this]
//                }
//                val size = s.actions.size
//                for (a in s.actions) {
//                    policy[s, a] = when {
//                        a === `a*` -> 1 - epsilon + epsilon / size
//                        else -> epsilon / size
//                    }
//                }
//            }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(policy, V, Q)
        V_from_Q_ND(states, result)
        return result
    }

    private fun updatePolicy(s: State, Q: ActionValueFunction, policy: NonDeterminedPolicy) {
        val `a*` = argmax(s.actions) {
            Q[s, this]
        }
        val size = s.actions.size
        for (a in s.actions) {
            policy[s, a] = when {
                a === `a*` -> 1 - epsilon + epsilon / size
                else -> epsilon / size
            }
        }
    }
}