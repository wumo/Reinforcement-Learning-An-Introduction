package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.ntd.NStepTemporalDifference.Companion.log
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.model.impl.mdp.StateValueFunction
import lab.mars.rl.model.isTerminal
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.Σ
import org.apache.commons.math3.util.FastMath.min
import org.apache.commons.math3.util.FastMath.pow

fun NStepTemporalDifference.prediction(): StateValueFunction {
    var n = n
    val V = indexedMdp.VFunc { 0.0 }
    val _R = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<IndexedState>(min(n, MAX_N))
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var T = Int.MAX_VALUE
        var t = 0
        var s = started()
        _R.clear();_R.append(0.0)
        _S.clear();_S.append(s)

        do {
            if (t >= n) {//最多存储n个
                _R.removeFirst(1)
                _S.removeFirst(1)
            }
            if (t < T) {
                val a = initial_policy(s)
                val (s_next, reward) = a.sample()
                _R.append(reward)
                _S.append(s_next)
                s = s_next
                if (s.isTerminal()) {
                    T = t + 1
                    val _t = t - n + 1
                    if (_t < 0) n = T //n is too large, normalize it
                }
            }
            val τ = t - n + 1

            if (τ >= 0) {
                var G = Σ(1..min(n, T - τ)) { pow(γ, it - 1) * _R[it] }
                if (τ + n < T) G += pow(γ, n) * V[_S[n]]
                V[_S[0]] += α * (G - V[_S[0]])
            }
            t++
        } while (τ < T - 1)
        log.debug { "n=$n,T=$T" }
    }
    return V
}