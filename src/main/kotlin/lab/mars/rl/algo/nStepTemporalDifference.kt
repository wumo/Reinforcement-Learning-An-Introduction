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
        val _R = newBuf<Double>(min(n, MAX_N))
        val _S = newBuf<State>(min(n, MAX_N))
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
            _R.clear();_R.append(0.0)
            _S.clear();_S.append(s)

            do {
                if (t >= n) {//最多存储n个
                    _R.removeFirst(1)
                    _S.removeFirst(1)
                }
                if (t < T) {
                    val a = s.actions.rand(policy(s))
                    val possible = a.sample()
                    _R.append(possible.reward)
                    _S.append(possible.next)
                    s = possible.next
                    if (s.isTerminal()) {
                        T = t + 1
                        val _t = t - n + 1
                        if (_t < 0) n = T //n is too large, normalize it
                    }
                }
                val _t = t - n + 1

                if (_t >= 0) {
                    var G = Sigma(1, min(n, T - _t)) { pow(gamma, it - 1) * _R[it] }
                    if (_t + n < T) G += pow(gamma, n) * V[_S[n]]
                    V[_S[0]] += alpha * (G - V[_S[0]])
                }
                t++
            } while (_t < T - 1)
            log.debug { "n=$n,T=$T" }
        }
        return V
    }

    fun sarsa(alpha: (State, Action) -> Double = { _, _ -> this.alpha }): OptimalSolution {
        val policy = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { 0.0 }
        val _R = newBuf<Double>(min(n, MAX_N))
        val _S = newBuf<State>(min(n, MAX_N))
        val _A = newBuf<Action>(min(n, MAX_N))

        for (episode in 1..episodes) {
            var n = n
            log.debug { "$episode/$episodes" }
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
            updatePolicy(s, Q, policy)
            var a = s.actions.rand(policy(s))
            _R.clear();_R.append(0.0)
            _S.clear();_S.append(s)
            _A.clear();_A.append(a)
            do {
                if (t >= n) {//最多存储n个
                    _R.removeFirst()
                    _S.removeFirst()
                    _A.removeFirst()
                }
                if (t < T) {
                    val possible = a.sample()
                    _R.append(possible.reward)
                    _S.append(possible.next)
                    s = possible.next
                    if (s.isTerminal()) {
                        T = t + 1
                        val _t = t - n + 1
                        if (_t < 0) n = T //n is too large, normalize it
                    } else {
                        updatePolicy(s, Q, policy)
                        a = s.actions.rand(policy(s))
                        _A.append(a)
                    }
                }
                val _t = t - n + 1
                if (_t >= 0) {
                    var G = Sigma(1, min(n, T - _t)) { pow(gamma, it - 1) * _R[it] }
                    if (_t + n < T) G += pow(gamma, n) * Q[_S[n], _A[n]]
                    Q[_S[0], _A[0]] += alpha(_S[0], _A[0]) * (G - Q[_S[0], _A[0]])
                    updatePolicy(states[_S[0]], Q, policy)
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
                a === `a*` -> 1.0 - epsilon + epsilon / size
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

    fun `off-policy sarsa`(alpha: (State, Action) -> Double = { _, _ -> this.alpha }): OptimalSolution {
        val b = mdp.equiprobablePolicy()
        val pi = b.copy()

        val Q = mdp.QFunc { 0.0 }
        val _R = newBuf<Double>(min(n, MAX_N))
        val _S = newBuf<State>(min(n, MAX_N))
        val _A = newBuf<Action>(min(n, MAX_N))

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var n = n
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
//            updatePolicy(s, Q, pi)
            var a = s.actions.rand(b(s))
            _R.clear();_R.append(0.0)
            _S.clear();_S.append(s)
            _A.clear();_A.append(a)
            do {
                if (t >= n) {//最多存储n个
                    _R.removeFirst()
                    _S.removeFirst()
                    _A.removeFirst()
                }
                if (t < T) {
                    val possible = a.sample()
                    _R.append(possible.reward)
                    _S.append(possible.next)
                    s = possible.next
                    if (s.isTerminal()) {
                        T = t + 1
                        val _t = t - n + 1
                        if (_t < 0) n = T //n is too large, normalize it
                    } else {
//                        updatePolicy(s, Q, pi)
                        a = s.actions.rand(b(s))
                        _A.append(a)
                    }
                }
                val _t = t - n + 1
                if (_t >= 0) {
                    val p = Pi(1, min(n - 1, T - 1 - _t)) { pi[_S[it], _A[it]] / b[_S[it], _A[it]] }
                    var G = Sigma(1, min(n, T - _t)) { pow(gamma, it - 1) * _R[it] }
                    if (_t + n < T) G += pow(gamma, n) * Q[_S[n], _A[n]]
                    Q[_S[0], _A[0]] += alpha(_S[0], _A[0]) * p * (G - Q[_S[0], _A[0]])
                    updatePolicy(states[_S[0]], Q, pi)
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

    fun treebackup(alpha: (State, Action) -> Double = { _, _ -> this.alpha }): OptimalSolution {
        val pi = mdp.equiprobablePolicy()
        val Q = mdp.QFunc { 0.0 }

        val _Q = newBuf<Double>(min(n, MAX_N))
        val _Pi = newBuf<Double>(min(n, MAX_N))
        val _delta = newBuf<Double>(min(n, MAX_N))
        val _S = newBuf<State>(min(n, MAX_N))
        val _A = newBuf<Action>(min(n, MAX_N))

        for (episode in 1..episodes) {
            var n = n
            log.debug { "$episode/$episodes" }
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
            var a = s.actions.rand(pi(s))

            _Q.clear(); _Q.append(0.0)
            _Pi.clear();_Pi.append(pi[s, a])
            _delta.clear()
            _S.clear();_S.append(s)
            _A.clear(); _A.append(a)

            do {
                if (t >= n) {//最多存储n个
                    _Q.removeFirst()
                    _Pi.removeFirst()
                    _delta.removeFirst()
                    _S.removeFirst()
                    _A.removeFirst()
                }
                if (t < T) {
                    val possible = a.sample()
                    _S.append(possible.next)
                    s = possible.next
                    val r = possible.reward
                    if (s.isTerminal()) {
                        _delta.append(r - _Q.last)
                        T = t + 1
                        val _t = t - n + 1
                        if (_t < 0) n = T //n is too large, normalize it
                    } else {
                        _delta.append(r + gamma * Sigma(s.actions) { pi[s, it] * Q[s, it] } - _Q.last)
                        a = s.actions.rand()
                        _A.append(a)
                        _Q.append(Q[s, a])
                        _Pi.append(pi[s, a])
                    }
                }
                val _t = t - n + 1
                if (_t >= 0) {
                    var e = 1.0
                    var G = _Q[0]
                    val end = min(n - 1, T - 1 - _t)
                    for (k in 0..end) {
                        G += e * _delta[k]
                        if (k < end) e *= gamma * _Pi[k + 1]
                    }
                    Q[_S[0], _A[0]] += alpha(_S[0], _A[0]) * (G - Q[_S[0], _A[0]])
                    updatePolicy(states[_S[0]], Q, pi)
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

    var sig: (Int) -> Int = { 0 }

    fun `off-policy Q sigma`(alpha: (State, Action) -> Double = { _, _ -> this.alpha }): OptimalSolution {
        val b = mdp.equiprobablePolicy()
        val pi = mdp.equiprobablePolicy()
        val Q = mdp.QFunc { 0.0 }

        val _Q = newBuf<Double>(min(n, MAX_N))
        val _Pi = newBuf<Double>(min(n, MAX_N))
        val _P = newBuf<Double>(min(n, MAX_N))
        val _sig = newBuf<Int>(min(n, MAX_N))
        val _delta = newBuf<Double>(min(n, MAX_N))
        val _S = newBuf<State>(min(n, MAX_N))
        val _A = newBuf<Action>(min(n, MAX_N))

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var n = n
            var T = Int.MAX_VALUE
            var t = 0
            var s = started.rand()
            var a = s.actions.rand(b(s))

            _Q.clear(); _Q.append(0.0)
            _Pi.clear(); _Pi.append(pi[s, a])
            _P.clear();_P.append(pi[s, a] / b[s, a])
            _sig.clear(); _sig.append(sig(0))
            _delta.clear()
            _S.clear();_S.append(s)
            _A.clear();_A.append(a)

            do {
                if (t >= n) {//最多存储n个
                    _Q.removeFirst()
                    _Pi.removeFirst()
                    _P.removeFirst()
                    _sig.removeFirst()
                    _delta.removeFirst()
                    _S.removeFirst()
                    _A.removeFirst()
                }
                if (t < T) {
                    val possible = a.sample()
                    _S.append(possible.next)
                    s = possible.next
                    val r = possible.reward
                    if (s.isTerminal()) {
                        _delta.append(r - _Q.last)
                        T = t + 1
                        val _t = t - n + 1
                        if (_t < 0) n = T //n is too large, normalize it
                    } else {
                        a = s.actions.rand(b(s));_A.append(a)
                        val sig = sig(t + 1)
                        _sig.append(sig)
                        _delta.append(r + gamma * sig * Q[s, a] + gamma * (1 - sig) * Sigma(s.actions) { pi[s, it] * Q[s, it] } - _Q.last)
                        _Q.append(Q[s, a])
                        _Pi.append(pi[s, a])
                        _P.append(pi[s, a] / b[s, a])
                    }
                }
                val _t = t - n + 1
                if (_t >= 0) {
                    var p = 1.0
                    var e = 1.0
                    var G = _Q[0]
                    val end = min(n - 1, T - 1 - _t)
                    for (k in 0..end) {
                        G += e * _delta[k]
                        if (k < end) e *= gamma * ((1 - _sig[k + 1]) * _Pi[k + 1] + _sig[k + 1])
                        p *= 1 - _sig[k] + _sig[k] * _P[k]
                    }
                    Q[_S[0], _A[0]] += alpha(_S[0], _A[0]) * p * (G - Q[_S[0], _A[0]])
                    updatePolicy(states[_S[0]], Q, pi)
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