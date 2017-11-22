package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.algo.ntd.NStepTemporalDifference.Companion.log
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isTerminal
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.Σ
import lab.mars.rl.util.tuples.tuple3
import org.apache.commons.math3.util.FastMath.min

fun NStepTemporalDifference.`off-policy n-step Q(σ)`(alpha: (IndexedState, IndexedAction) -> Double = { _, _ -> this.α }): OptimalSolution {
    val b = indexedMdp.equiprobablePolicy()
    val π =indexedMdp.equiprobablePolicy()
    val Q = indexedMdp.QFunc { 0.0 }

    val _Q = newBuf<Double>(min(n, MAX_N))
    val _π = newBuf<Double>(min(n, MAX_N))
    val ρ = newBuf<Double>(min(n, MAX_N))
    val _σ = newBuf<Int>(min(n, MAX_N))
    val δ = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<IndexedState>(min(n, MAX_N))
    val _A = newBuf<IndexedAction>(min(n, MAX_N))

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var n = n
        var T = Int.MAX_VALUE
        var t = 0
        var s = started.rand()
        var a =b(s)

        _Q.clear(); _Q.append(0.0)
        _π.clear(); _π.append(π[s, a])
        ρ.clear();ρ.append(π[s, a] / b[s, a])
        _σ.clear(); _σ.append(σ(0))
        δ.clear()
        _S.clear();_S.append(s)
        _A.clear();_A.append(a)

        do {
            if (t >= n) {//最多存储n个
                _Q.removeFirst()
                _π.removeFirst()
                ρ.removeFirst()
                _σ.removeFirst()
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
                    a =b(s);_A.append(a)
                    val tmp_σ = σ(t + 1)
                    _σ.append(tmp_σ)
                    δ.append(reward + γ * tmp_σ * Q[s, a] + γ * (1 - tmp_σ) * Σ(s.actions) { π[s, it] * Q[s, it] } - _Q.last)
                    _Q.append(Q[s, a])
                    _π.append(π[s, a])
                    ρ.append(π[s, a] / b[s, a])
                }
            }
            val τ = t - n + 1
            if (τ >= 0) {
                var _ρ = 1.0
                var Z = 1.0
                var G = _Q[0]
                val end = min(n - 1, T - 1 - τ)
                for (k in 0..end) {
                    G += Z * δ[k]
                    if (k < end) Z *= γ * ((1 - _σ[k + 1]) * _π[k + 1] + _σ[k + 1])
                    _ρ *= 1 - _σ[k] + _σ[k] * ρ[k]
                }
                Q[_S[0], _A[0]] += alpha(_S[0], _A[0]) * _ρ * (G - Q[_S[0], _A[0]])
                `ε-greedy`(_S[0], Q, π, ε)
            }
            t++
        } while (τ < T - 1)
        log.debug { "n=$n,T=$T" }
    }
    val V = indexedMdp.VFunc { 0.0 }
    val result = tuple3(π, V, Q)
    V_from_Q(states, result)
    return result
}