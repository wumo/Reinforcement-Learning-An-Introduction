@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.off_policy

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.ntd.MAX_N
import lab.mars.rl.algo.ntd.NStepTemporalDifference.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.Σ
import lab.mars.rl.util.matrix.times
import org.apache.commons.math3.util.FastMath.min

fun<E> FunctionApprox.`off-policy n-step Q(σ) episodic`(n: Int, b: Policy, σ: (Int) -> Int = { 0 },
                                                     q: ApproximateFunction<E>, trans: (State, Action<State>) -> E) {
    val _Q = newBuf<Double>(min(n, MAX_N))
    val _π = newBuf<Double>(min(n, MAX_N))
    val ρ = newBuf<Double>(min(n, MAX_N))
    val _σ = newBuf<Int>(min(n, MAX_N))
    val δ = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<State>(min(n, MAX_N))
    val _A = newBuf<Action<State>>(min(n, MAX_N))

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var n = n
        var T = Int.MAX_VALUE
        var t = 0
        var s = started()
        var a = b(s)

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
                    a = b(s);_A.append(a)
                    val tmp_σ = σ(t + 1)
                    _σ.append(tmp_σ)
                    δ.append(reward + γ * tmp_σ * q(trans(s, a)) + γ * (1 - tmp_σ) * Σ(s.actions) { π[s, it] * q(trans(s, it)) } - _Q.last)
                    _Q.append(q(trans(s, a)))
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
                q.w += α * _ρ * (G - q(trans(_S[0], _A[0]))) * q.`▽`(trans(_S[0], _A[0]))
            }
            t++
        } while (τ < T - 1)
        log.debug { "n=$n,T=$T" }
    }
}

fun <E> FunctionApprox.`off-policy n-step Q(σ) continuing`(n: Int, b: Policy, σ: (Int) -> Int = { 0 }, β: Double
                                                           , q: ApproximateFunction<E>, trans: (State, Action<State>) -> E) {
    var average_reward = 0.0
    val _Q = newBuf<Double>(min(n, MAX_N))
    val _π = newBuf<Double>(min(n, MAX_N))
    val ρ = newBuf<Double>(min(n, MAX_N))
    val _σ = newBuf<Int>(min(n, MAX_N))
    val δ = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<State>(min(n, MAX_N))
    val _A = newBuf<Action<State>>(min(n, MAX_N))

    var t = 0
    var s = started()
    var a = b(s)

    _Q.clear(); _Q.append(0.0)
    _π.clear(); _π.append(π[s, a])
    ρ.clear();ρ.append(π[s, a] / b[s, a])
    _σ.clear(); _σ.append(σ(0))
    δ.clear()
    _S.clear();_S.append(s)
    _A.clear();_A.append(a)

    while (true) {
        if (t >= n) {//最多存储n个
            _Q.removeFirst()
            _π.removeFirst()
            ρ.removeFirst()
            _σ.removeFirst()
            δ.removeFirst()
            _S.removeFirst()
            _A.removeFirst()
        }
        val (s_next, reward) = a.sample()
        _S.append(s_next)
        s = s_next
        a = b(s);_A.append(a)
        val tmp_σ = σ(t + 1)
        _σ.append(tmp_σ)
        val _δ = reward - average_reward + tmp_σ * q(trans(s, a)) + (1 - tmp_σ) * Σ(s.actions) { π[s, it] * q(trans(s, it)) } - _Q.last
        δ.append(_δ)
        average_reward += β * _δ
        _Q.append(q(trans(s, a)))
        _π.append(π[s, a])
        ρ.append(π[s, a] / b[s, a])
        val τ = t - n + 1
        if (τ >= 0) {
            var _ρ = 1.0
            var Z = 1.0
            var G = _Q[0]
            val end = n - 1
            for (k in 0..end) {
                G += Z * δ[k]
                if (k < end) Z *= (1 - _σ[k + 1]) * _π[k + 1] + _σ[k + 1]
                _ρ *= 1 - _σ[k] + _σ[k] * ρ[k]
            }
            q.w += α * _ρ * (G - q(trans(_S[0], _A[0]))) * q.`▽`(trans(_S[0], _A[0]))
        }
        t++
    }
}