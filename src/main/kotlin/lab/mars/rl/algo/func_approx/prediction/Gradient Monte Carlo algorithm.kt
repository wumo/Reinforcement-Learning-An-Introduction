package lab.mars.rl.algo.func_approx.prediction

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.times

fun <E> FunctionApprox.`Gradient Monte Carlo algorithm`(v: ApproximateFunction<E>) {
    val _S = newBuf<State>()
    val _R = newBuf<Double>()

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var step = 0
        _S.clear(); _R.clear()
        var s = started()
        _S.append(s); _R.append(0.0)
        var T = 0
        var accum = 0.0
        while (s.isNotTerminal()) {
            step++
            val a = π(s)
            val (s_next, reward) = a.sample()
            accum += reward
            _S.append(s_next)
            _R.append(reward)
            s = s_next
            T++
        }
        var pre = 0.0
        for (t in 0 until T) {
            pre += _R[t]
            val Gt = accum - pre
            v.w += α * (Gt - v(_S[t])) * v.`▽`(_S[t])
        }
        episodeListener(episode, step)
    }
}