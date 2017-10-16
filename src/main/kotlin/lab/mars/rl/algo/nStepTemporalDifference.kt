package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.*
import lab.mars.rl.util.buf.newBuf
import org.apache.commons.math3.util.FastMath.min
import org.apache.commons.math3.util.FastMath.pow
import org.slf4j.LoggerFactory

const val MAX_N = 1024

/**
 * <p>
 * Created on 2017-10-09.
 * </p>
 *
 * @author wumo
 */
class nStepTemporalDifference(val mdp: MDP, val n: Int, private var policy: NonDeterminedPolicy = emptyNSet()) {
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
        var n = n
        val V = mdp.VFunc { 0.0 }
        val R = newBuf<Double>(min(n, MAX_N))
        val S = newBuf<State>(min(n, MAX_N))
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
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
                    if (s.isTerminal()) {
                        T = t + 1
                        val _t = t - n + 1
                        if (_t < 0) n = T //n is too large, normalize it
                    }
                }
                val _t = t - n + 1

                if (_t >= 0) {
                    var G = sigma(_t + 1, min(_t + n, T)) { pow(gamma, it - _t - 1) * R[it - _t] }
                    if (_t + n < T) G += pow(gamma, n) * V[S[n]]
                    V[S[0]] += alpha * (G - V[S[0]])
                }
                t++
            } while (_t < T - 1)
            log.debug { "n=$n,T=$T" }
        }
        return V
    }

    fun sarsa(): OptimalSolution {
        var n = n
        val policy = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { 0.0 }
        val R = newBuf<Double>(min(n, MAX_N))
        val S = newBuf<State>(min(n, MAX_N))
        val A = newBuf<Action>(min(n, MAX_N))

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
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
                    if (s.isTerminal()) {
                        T = t + 1
                        val _t = t - n + 1
                        if (_t < 0) n = T //n is too large, normalize it
                    } else {
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
                    updatePolicy(states[S[0]], Q, policy)
                }
                t++
            } while (_t < T - 1)
            log.debug { "n=$n,T=$T" }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(policy, V, Q)
        V_from_Q_ND(states, result)
        return result
    }

    private fun updatePolicy(s: State, Q: ActionValueFunction, policy: NonDeterminedPolicy) {
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
        TODO()
    }

    fun expectedSarsa(): OptimalSolution {
        TODO()
    }

    fun DoubleQLearning(): OptimalSolution {
        TODO()
    }

}