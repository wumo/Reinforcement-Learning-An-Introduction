package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.algo.ntd.NStepTemporalDifference.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.debug
import lab.mars.rl.util.tuples.tuple3
import lab.mars.rl.util.Σ
import org.apache.commons.math3.util.FastMath.min

fun NStepTemporalDifference.treebackup(alpha: (IndexedState, IndexedAction) -> Double = { _, _ -> this.α }): OptimalSolution {
    val π = indexedMdp.equiprobablePolicy()
    val Q = indexedMdp.QFunc { 0.0 }

    val _Q = newBuf<Double>(min(n, MAX_N))
    val _π = newBuf<Double>(min(n, MAX_N))
    val δ = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<IndexedState>(min(n, MAX_N))
    val _A = newBuf<IndexedAction>(min(n, MAX_N))

    for (episode in 1..episodes) {
        var n = n
        log.debug { "$episode/$episodes" }
        var T = Int.MAX_VALUE
        var t = 0
        var s = started.rand()
        var a = s.actions.rand(π(s))

        _Q.clear(); _Q.append(0.0)
        _π.clear();_π.append(π[s, a])
        δ.clear()
        _S.clear();_S.append(s)
        _A.clear(); _A.append(a)

        do {
            if (t >= n) {//最多存储n个
                _Q.removeFirst()
                _π.removeFirst()
                δ.removeFirst()
                _S.removeFirst()
                _A.removeFirst()
            }
            if (t < T) {
                val (s_next, reward) = a.sample()
                _S.append(s_next)
                s = s_next
                if (s.isTerminal()) {
                    δ.append(reward - _Q.last)
                    T = t + 1
                    val _t = t - n + 1
                    if (_t < 0) n = T //n is too large, normalize it
                } else {
                    δ.append(reward + γ * Σ(s.actions) { π[s, it] * Q[s, it] } - _Q.last)
                    a = s.actions.rand()
                    _A.append(a)
                    _Q.append(Q[s, a])
                    _π.append(π[s, a])
                }
            }
            val τ = t - n + 1
            if (τ >= 0) {
                var Z = 1.0
                var G = _Q[0]
                val end = min(n - 1, T - 1 - τ)
                for (k in 0..end) {
                    G += Z * δ[k]
                    if (k < end) Z *= γ * _π[k + 1]
                }
                Q[_S[0], _A[0]] += alpha(_S[0], _A[0]) * (G - Q[_S[0], _A[0]])
                `ε-greedy`(_S[0], Q, π, ε)
            }
            t++
        } while (τ < T - 1)
        log.debug { "n=$n,T=$T" }
    }
    val V = indexedMdp.VFunc { 0.0 }
    val result = tuple3(π, V, Q)
    V_from_Q_ND(states, result)
    return result
}