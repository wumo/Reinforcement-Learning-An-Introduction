@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.on_policy_control

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.ntd.MAX_N
import lab.mars.rl.model.*
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.math.Σ
import lab.mars.rl.util.matrix.times
import org.apache.commons.math3.util.FastMath.min

fun FunctionApprox.`Differential semi-gradient n-step Sarsa`(qFunc: ActionValueApproxFunction, n: Int, β: Double) {
    var average_reward = 0.0
    val _R = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<State>(min(n, MAX_N))
    val _A = newBuf<Action<State>>(min(n, MAX_N))

    var t = 0
    val s = started.rand()
    π.`ε-greedy update`(s, qFunc)
    var a = π(s)
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
        π.`ε-greedy update`(s_next, qFunc)
        a = π(s)
        _A.append(a)
        val τ = t - n + 1
        if (τ >= 0) {
            val δ = Σ(1..n) { _R[it] - average_reward } + qFunc(_S[n], _A[n]) - qFunc(_S[0], _A[0])
            average_reward += β * δ
            qFunc.w += α * δ * qFunc.`▽`(_S[0], _A[0])
        }
        t++
    }
}