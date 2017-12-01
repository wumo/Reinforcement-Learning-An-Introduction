package lab.mars.rl.algo.func_approx.prediction

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.*

fun <E> FunctionApprox.LSTD(vFunc: LinearFunc<E>, trans: (State) -> E, ε: Double) {
    val xFeature = vFunc.x
    val d = xFeature.numOfComponents
    val A_ = 1 / ε * Matrix.identity(d)
    val b = Matrix.column(d)
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var step = 0
        var s = started()
        var x = xFeature(trans(s))
        while (s.isNotTerminal()) {
            step++
            val a = π(s)
            val (s_next, reward) = a.sample()
            val _x = if (s_next.isTerminal()) Matrix.column(d) else xFeature(trans(s_next))

            val v = A_.T * (x - γ * _x)
            A_ -= (A_ * x) * v.T / (1.0 + v.T * x)
            b += reward * x
            s = s_next
            x = _x
        }
        episodeListener(episode, step)
    }
    vFunc.w `=` A_ * b
}