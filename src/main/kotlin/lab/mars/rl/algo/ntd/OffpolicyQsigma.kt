package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.ntd.NStepTemporalDifference.Companion.log
import lab.mars.rl.model.Action
import lab.mars.rl.model.OptimalSolution
import lab.mars.rl.model.State
import lab.mars.rl.util.Sigma
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.debug
import org.apache.commons.math3.util.FastMath.min

fun NStepTemporalDifference.`off-policy Q sigma`(alpha: (State, Action) -> Double = { _, _ -> this.alpha }): OptimalSolution {
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