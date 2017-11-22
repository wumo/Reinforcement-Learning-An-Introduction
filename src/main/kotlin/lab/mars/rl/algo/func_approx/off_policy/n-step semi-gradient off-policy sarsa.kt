@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.off_policy

import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.algo.ntd.MAX_N
import lab.mars.rl.model.*
import lab.mars.rl.util.*
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.matrix.times
import org.apache.commons.math3.util.FastMath.min
import org.apache.commons.math3.util.FastMath.pow

fun FunctionApprox.`n-step semi-gradient off-policy sarsa episodic`(n: Int, q: ActionValueApproxFunction, b: NonDeterminedPolicy) {
    val _R = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<IndexedState>(min(n, MAX_N))
    val _A = newBuf<IndexedAction>(min(n, MAX_N))

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var n = n
        var T = Int.MAX_VALUE
        var t = 0
        var s = started.rand()
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
                val (s_next, reward) = a.sample()
                _R.append(reward)
                _S.append(s_next)
                s = s_next
                if (s.isTerminal()) {
                    T = t + 1
                    val _t = t - n + 1
                    if (_t < 0) n = T //n is too large, normalize it
                } else {
                    a = s.actions.rand(b(s))
                    _A.append(a)
                }
            }
            val τ = t - n + 1
            if (τ >= 0) {
                val ρ = Π(1..min(n - 1, T - 1 - τ)) { π[_S[it], _A[it]] / b[_S[it], _A[it]] }
                var G = Σ(1..min(n, T - τ)) { pow(γ, it - 1) * _R[it] }
                if (τ + n < T) G += pow(γ, n) * q(_S[n], _A[n])
                q.w += α * ρ * (G - q(_S[0], _A[0])) * q.`▽`(_S[0], _A[0])
                `ε-greedy`(_S[0], q, π, ε)
            }
            t++
        } while (τ < T - 1)
        log.debug { "n=$n,T=$T" }
        episodeListener(episode)
    }
}

fun FunctionApprox.`n-step semi-gradient off-policy sarsa continuing`(n: Int, q: ActionValueApproxFunction, b: NonDeterminedPolicy, β: Double) {
    var average_reward = 0.0
    val _R = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<IndexedState>(min(n, MAX_N))
    val _A = newBuf<IndexedAction>(min(n, MAX_N))

    var t = 0
    val s = started.rand()
    var a = s.actions.rand(b(s))
    _R.clear();_R.append(0.0)
    _S.clear();_S.append(s)
    _A.clear();_A.append(a)
    while (true) {
        if (t >= n) {//最多存储n个
            _R.removeFirst()
            _S.removeFirst()
            _A.removeFirst()
        }
        val (s_next, reward) = a.sample()
        _R.append(reward)
        _S.append(s_next)
        a = s.actions.rand(b(s))
        _A.append(a)

        val τ = t - n + 1
        if (τ >= 0) {
            val ρ = Π(1..n) { π[_S[it], _A[it]] / b[_S[it], _A[it]] }
            val δ = Σ(1..n) { _R[it] - average_reward } + q(_S[n], _A[n]) - q(_S[0], _A[0])
            average_reward += β * δ
            q.w += α * ρ * δ * q.`▽`(_S[0], _A[0])
            `ε-greedy`(_S[0], q, π, ε)
        }
        t++
    }
}