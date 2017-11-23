@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.prediction

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.algo.ntd.MAX_N
import lab.mars.rl.model.*
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.Σ
import lab.mars.rl.util.matrix.times
import org.apache.commons.math3.util.FastMath.min
import org.apache.commons.math3.util.FastMath.pow

fun <E> FunctionApprox.`n-step semi-gradient TD`(n: Int, v: ApproximateFunction<E>, trans: (State) -> E) {
    val _R = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<State>(min(n, MAX_N))
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var n = n
        var T = Int.MAX_VALUE
        var t = 0
        var s = started()
        var a = π(s)
        _R.clear();_R.append(0.0)
        _S.clear();_S.append(s)
        do {
            if (t >= n) {//最多存储n个
                _R.removeFirst()
                _S.removeFirst()
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
                } else
                    a = π(s)
            }
            val τ = t - n + 1
            if (τ >= 0) {
                var G = Σ(1..min(n, T - τ)) { pow(γ, it - 1) * _R[it] }
                if (τ + n < T) G += pow(γ, n) * v(trans(_S[n]))
                v.w += α * (G - v(trans(_S[0]))) * v.`▽`(trans(_S[0]))
            }
            t++
        } while (τ < T - 1)
        log.debug { "n=$n,T=$T" }
        episodeListener(episode)
    }
}