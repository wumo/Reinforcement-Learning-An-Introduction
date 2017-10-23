package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.ntd.NStepTemporalDifference.Companion.log
import lab.mars.rl.model.State
import lab.mars.rl.model.StateValueFunction
import lab.mars.rl.util.Sigma
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.debug
import org.apache.commons.math3.util.FastMath.min
import org.apache.commons.math3.util.FastMath.pow


fun NStepTemporalDifference.prediction(): StateValueFunction {
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
                val a = s.actions.rand(initial_policy(s))
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