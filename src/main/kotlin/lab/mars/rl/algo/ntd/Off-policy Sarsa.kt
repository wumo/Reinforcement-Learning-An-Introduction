package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.algo.ntd.NStepTemporalDifference.Companion.log
import lab.mars.rl.model.Action
import lab.mars.rl.model.OptimalSolution
import lab.mars.rl.model.State
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.debug
import lab.mars.rl.util.Π
import lab.mars.rl.util.Σ
import lab.mars.rl.util.tuples.tuple3
import org.apache.commons.math3.util.FastMath.min
import org.apache.commons.math3.util.FastMath.pow

fun NStepTemporalDifference.`off-policy sarsa`(alpha: (State, Action) -> Double = { _, _ -> this.α }): OptimalSolution {
    val b = mdp.equiprobablePolicy()
    val π = b.copy()

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
                val (s_next, reward, _) = a.sample()
                _R.append(reward)
                _S.append(s_next)
                s = s_next
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
            val τ = t - n + 1
            if (τ >= 0) {
                val ρ = Π(1.. min(n - 1, T - 1 - τ)) { π[_S[it], _A[it]] / b[_S[it], _A[it]] }
                var G = Σ(1.. min(n, T - τ)) { pow(γ, it - 1) * _R[it] }
                if (τ + n < T) G += pow(γ, n) * Q[_S[n], _A[n]]
                Q[_S[0], _A[0]] += alpha(_S[0], _A[0]) * ρ * (G - Q[_S[0], _A[0]])
                `ε-greedy`(states[_S[0]], Q, π, ε)
            }
            t++
        } while (τ < T - 1)
        log.debug { "n=$n,T=$T" }
    }
    val V = mdp.VFunc { 0.0 }
    val result = tuple3(π, V, Q)
    V_from_Q_ND(states, result)
    return result
}