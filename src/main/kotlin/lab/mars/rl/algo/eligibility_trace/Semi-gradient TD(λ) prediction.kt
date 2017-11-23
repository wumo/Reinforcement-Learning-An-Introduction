package lab.mars.rl.algo.eligibility_trace

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.State
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.Matrix
import lab.mars.rl.util.matrix.times

fun <E> FunctionApprox.`Semi-gradient TD(λ) prediction`(vFunc: LinearFunc<E>, trans: (State) -> E, λ: Double) {
    val xFeature = vFunc.x
    val d = xFeature.numOfComponents
    var z = Matrix.column(d)
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started()
        while (s.isNotTerminal()) {
            val a = π(s)
            val (s_next, reward) = a.sample()
            z = γ * λ * z + vFunc.`▽`(trans(s))
            val δ = reward + γ * vFunc(trans(s_next)) - vFunc(trans(s))
            vFunc.w += α * δ * z
            s = s_next
        }
        episodeListener(episode)
    }
}