package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.*
import lab.mars.rl.util.buf.DefaultBuf
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.FastMath.min
import org.apache.commons.math3.util.FastMath.pow

/**
 * <p>
 * Created on 2017-10-09.
 * </p>
 *
 * @author wumo
 */
class nStepTemporalDifference(val mdp: MDP, val n: Int, private var policy: NonDeterminedPolicy = emptyNSet()) {
    val gamma = mdp.gamma
    val started = mdp.started
    val states = mdp.states
    var episodes = 10000
    var alpha = 0.1
    var epsilon = 0.1

    fun prediction(): StateValueFunction {
        val V = mdp.VFunc { 0.0 }
        val R = DefaultBuf.new<Double>(n)
        val S = DefaultBuf.new<State>(n)
        for (episode in 1..episodes) {
            println("$episode/$episodes")
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
            R.clear();R.append(0.0)
            S.clear();S.append(s)

            do {
                if (t >= n) {//最多存储n个
                    R.removeFirst(1)
                    S.removeFirst(1)
                }
                if (t < T) {
                    val a = s.actions.rand(policy(s))
                    val possible = a.sample()
                    R.append(possible.reward)
                    S.append(possible.next)
                    s = possible.next
                    if (s.isTerminal()) T = t + 1
                }
                val _t = t - n + 1
                if (_t >= 0) {
                    var G = sigma(_t + 1, min(_t + n, T)) { pow(gamma, it - _t - 1) * R[it - _t] }
                    if (_t + n < T) G += pow(gamma, n) * V[S[n]]
                    V[S[0]] += alpha * (G - V[S[0]])
                }
                t++
            } while (_t < T - 1)
        }
        return V
    }

    fun sarsa(): OptimalSolution {
        val policy = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { 0.0 }
        val R = DefaultBuf.new<Double>(n)
        val S = DefaultBuf.new<State>(n)
        val A = DefaultBuf.new<Action>(n)

        for (episode in 1..episodes) {
            println("$episode/$episodes")
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
            updatePolicy(s, Q, policy)
            var a = s.actions.rand(policy(s))
            R.clear();R.append(0.0)
            S.clear();S.append(s)
            A.clear();A.append(a)
            do {
                if (t >= n) {//最多存储n个
                    R.removeFirst(1)
                    S.removeFirst(1)
                    A.removeFirst(1)
                }
                if (t < T) {
                    val possible = a.sample()
                    R.append(possible.reward)
                    S.append(possible.next)
                    s = possible.next
                    if (s.isTerminal()) T = t + 1
                    else {
                        updatePolicy(s, Q, policy)
                        a = s.actions.rand(policy(s))
                        A.append(a)
                    }
                }
                val _t = t - n + 1
                if (_t >= 0) {
                    var G = sigma(_t + 1, min(_t + n, T)) { pow(gamma, it - _t - 1) * R[it - _t] }
                    if (_t + n < T) G += pow(gamma, n) * Q[S[n], A[n]]
                    Q[S[0], A[0]] += alpha * (G - Q[S[0], A[0]])
                    updatePolicy(states[S[0]],Q,policy)
                }
                t++
            } while (_t < T - 1)
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

    fun QLearning(): OptimalSolution {
        val policy = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { 0.0 }

        for (episode in 1..episodes) {
            println("$episode/$episodes")
            var s = started.rand()
            while (true) {
                updatePolicy(s, Q, policy)
                val a = s.actions.rand(policy(s))
                val possible = a.sample()
                val s_next = possible.next
                if (s_next.isNotTerminal()) {
                    Q[s, a] += alpha * (possible.reward + gamma * max(s_next.actions) { Q[s_next, this] } - Q[s, a])
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
            println("$episode/$episodes")
            var s = started.rand()
            while (true) {
                updatePolicy(s, Q, policy)
                val a = s.actions.rand(policy(s))
                val possible = a.sample()
                val s_next = possible.next
                if (s_next.isNotTerminal()) {
                    Q[s, a] += alpha * (possible.reward + gamma * sigma(s_next.actions) { policy[s_next, this] * Q[s_next, this] } - Q[s, a])
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
            println("$episode/$episodes")
            var s = started.rand()
            while (true) {
                updatePolicy(s, Q1, Q2, policy)
                val a = s.actions.rand(policy(s))
                val possible = a.sample()
                val s_next = possible.next
                if (Rand().nextBoolean()) {
                    val tmp = Q1
                    Q1 = Q2
                    Q2 = tmp
                }
                if (s_next.isNotTerminal()) {
                    Q1[s, a] += alpha * (possible.reward + gamma * Q2[s_next, argmax(s_next.actions) { Q1[s_next, this] }] - Q1[s, a])
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

    private fun updatePolicy(s: State, Q1: ActionValueFunction, Q2: ActionValueFunction, policy: NonDeterminedPolicy) {
        val `a*` = argmax(s.actions) {
            Q1[s, this] + Q2[s, this]
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