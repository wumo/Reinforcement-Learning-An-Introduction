@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.on_policy_control

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

fun FunctionApprox.`Episodic semi-gradient n-step Sarsa control`(qFunc: ActionValueApproxFunction, n: Int) {
    val _R = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<State>(min(n, MAX_N))
    val _A = newBuf<Action<State>>(min(n, MAX_N))

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var n = n
        var T = Int.MAX_VALUE
        var t = 0
        var s = started.rand()
        π.`ε-greedy update`(s, qFunc)
        var a = π(s)
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
                    π.`ε-greedy update`(s, qFunc)
                    a = π(s)
                    _A.append(a)
                }
            }
            val τ = t - n + 1
            if (τ >= 0) {
                var G = Σ(1..min(n, T - τ)) { pow(γ, it - 1) * _R[it] }
                if (τ + n < T) G += pow(γ, n) * qFunc(_S[n], _A[n])
                qFunc.w += α * (G - qFunc(_S[0], _A[0])) * qFunc.`▽`(_S[0], _A[0])
            }
            t++
        } while (τ < T - 1)
        log.debug { "n=$n,T=$T" }
    }
}