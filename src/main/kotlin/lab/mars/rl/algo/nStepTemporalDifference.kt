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
                    var G = Sigma(1, min(n, T - _t)) { pow(gamma, it - 1) * R[it] }
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
        val policy = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { 0.0 }
        val R = newBuf<Double>(min(n, MAX_N))
        val S = newBuf<State>(min(n, MAX_N))
        val A = newBuf<Action>(min(n, MAX_N))

        for (episode in 1..episodes) {
            var n = n
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
                    var G = Sigma(1, min(n, T - _t)) { pow(gamma, it - 1) * R[it] }
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

    fun `off-policy sarsa`(): OptimalSolution {
        val b = mdp.QFunc { 1.0 }
        for (s in states) {
            if (s.isTerminal()) continue
            val prob = 1.0 / s.actions.size
            for (a in s.actions)
                b[s, a] = prob
        }
        val pi = b.copy()

        val Q = mdp.QFunc { 0.0 }
        val R = newBuf<Double>(min(n, MAX_N))
        val S = newBuf<State>(min(n, MAX_N))
        val A = newBuf<Action>(min(n, MAX_N))

        for (episode in 1..episodes) {
            var n = n
            log.debug { "$episode/$episodes" }
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
            updatePolicy(s, Q, pi)
            var a = s.actions.rand(b(s))
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
                        updatePolicy(s, Q, pi)
                        a = s.actions.rand(b(s))
                        A.append(a)
                    }
                }
                val _t = t - n + 1
                if (_t >= 0) {
                    val p = Pi(1, min(n - 1, T - 1 - _t)) { pi[S[it], A[it]] / b[S[it], A[it]] }
                    var G = Sigma(1, min(n, T - _t)) { pow(gamma, it - 1) * R[it] }
                    if (_t + n < T) G += pow(gamma, n) * Q[S[n], A[n]]
                    Q[S[0], A[0]] += alpha * p * (G - Q[S[0], A[0]])
                    updatePolicy(states[S[0]], Q, pi)
                }
                t++
            } while (_t < T - 1)
            log.debug { "n=$n,T=$T" }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(pi, V, Q)
        V_from_Q_ND(states, result)
        return result
    }

    fun treebackup(): OptimalSolution {
        val pi = policy
        val Q = mdp.QFunc { 0.0 }
        val S = newBuf<State>(min(n, MAX_N))
        val A = newBuf<Action>(min(n, MAX_N))
        val delta = newBuf<Double>(min(n, MAX_N))
        val tmpQ = newBuf<Double>(min(n, MAX_N))
        val tmpPi = newBuf<Double>(min(n, MAX_N))

        for (episode in 1..episodes) {
            var n = n
            log.debug { "$episode/$episodes" }
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
            updatePolicy(s, Q, pi)
            var a = s.actions.rand(pi(s))
            tmpQ.clear(); tmpQ.append(0.0)
            tmpPi.clear(); tmpPi.append(0.0)
            S.clear();S.append(s)
            A.clear();A.append(a)
            do {
                if (t >= n) {//最多存储n个
                    tmpPi.removeFirst(1)
                    delta.removeFirst(1)
                    S.removeFirst(1)
                    A.removeFirst(1)
                }
                if (t < T) {
                    val possible = a.sample()
                    S.append(possible.next)
                    s = possible.next
                    val r = possible.reward
                    if (s.isTerminal()) {
                        delta.append(r - tmpQ.last)
                        T = t + 1
                        val _t = t - n + 1
                        if (_t < 0) n = T //n is too large, normalize it
                    } else {
                        delta.append(r + gamma * Sigma(s.actions) { pi[s, it] * Q[s, it] } - tmpQ.last)
                        a = s.actions.rand()
                        tmpQ.append(Q[s, a])
                        tmpPi.append(pi[s, a])
                        A.append(a)
                    }
                }
                val _t = t - n + 1
                if (_t >= 0) {
                    var e = 1.0
                    var G = Q[0]
                    val end = min(n - 1, T - 1 - _t)
                    for (k in 0..end) {
                        G += e * delta[k]
                        if (k < end) e *= gamma * tmpPi[k + 1]
                    }
                    Q[S[0], A[0]] += alpha * (G - Q[S[0], A[0]])
                    updatePolicy(states[S[0]], Q, pi)
                }
                t++
            } while (_t < T - 1)
            log.debug { "n=$n,T=$T" }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(pi, V, Q)
        V_from_Q_ND(states, result)
        return result
    }

    lateinit var sig: RandomAccessCollection<Int>

    fun `off-policy Q delta`(): OptimalSolution {
        val b = mdp.QFunc { 1.0 }
        for (s in states) {
            if (s.isTerminal()) continue
            val prob = 1.0 / s.actions.size
            for (a in s.actions)
                b[s, a] = prob
        }
        val pi = policy
        val Q = mdp.QFunc { 0.0 }
        val S = newBuf<State>(min(n, MAX_N))
        val A = newBuf<Action>(min(n, MAX_N))
        val delta = newBuf<Double>(min(n, MAX_N))
        val tmpQ = newBuf<Double>(min(n, MAX_N))
        val tmpPi = newBuf<Double>(min(n, MAX_N))
        val tmpP = newBuf<Double>(min(n, MAX_N))
        val tmpSig = newBuf<Int>(min(n, MAX_N))

        for (episode in 1..episodes) {
            var n = n
            log.debug { "$episode/$episodes" }
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
            updatePolicy(s, Q, pi)
            var a = s.actions.rand(b(s))
            tmpQ.clear(); tmpQ.append(0.0)
            tmpPi.clear(); tmpPi.append(0.0)
            tmpP.clear();tmpP.append(0.0)
            tmpSig.clear(); tmpSig.append(1)
            S.clear();S.append(s)
            A.clear();A.append(a)
            do {
                if (t >= n) {//最多存储n个
                    tmpPi.removeFirst(1)
                    delta.removeFirst(1)
                    S.removeFirst(1)
                    A.removeFirst(1)
                }
                if (t < T) {
                    val possible = a.sample()
                    S.append(possible.next)
                    s = possible.next
                    val r = possible.reward
                    if (s.isTerminal()) {
                        delta.append(r - tmpQ.last)
                        T = t + 1
                        val _t = t - n + 1
                        if (_t < 0) n = T //n is too large, normalize it
                    } else {
                        a = s.actions.rand(b(s));A.append(a)
                        val sig = sig[t + 1]
                        tmpSig.append(sig)
                        delta.append(r + gamma * sig * Q[s, a] + gamma * (1 - sig) * Sigma(s.actions) { pi[s, it] * Q[s, it] } - tmpQ.last)
                        tmpQ.append(Q[s, a])
                        tmpPi.append(pi[s, a])
                        tmpP.append(pi[s, a] / b[s, a])
                    }
                }
                val _t = t - n + 1
                if (_t >= 0) {
                    var p = 1.0
                    var e = 1.0
                    var G = Q[0]
                    val end = min(n - 1, T - 1 - _t)
                    for (k in 0..end) {
                        G += e * delta[k]
                        if (k < end) e *= gamma * ((1 - delta[k + 1]) * tmpPi[k + 1] + delta[k + 1])
                        p *= 1 - delta[k] + delta[k] * tmpP[k]
                    }
                    Q[S[0], A[0]] += alpha * p * (G - Q[S[0], A[0]])
                    updatePolicy(states[S[0]], Q, pi)
                }
                t++
            } while (_t < T - 1)
            log.debug { "n=$n,T=$T" }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(pi, V, Q)
        V_from_Q_ND(states, result)
        return result
    }
}