package lab.mars.rl.algo.eligibility_trace

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.impl.LinearFunc
import lab.mars.rl.util.debug
import lab.mars.rl.util.matrix.Matrix
import lab.mars.rl.util.matrix.times

fun FunctionApprox.`Semi-gradient TD(λ)`(vFunc: LinearFunc, `λ`: Double) {
    val xFeature = vFunc.x
    val d = xFeature.featureNum
    val z = Matrix.column(d)
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        while (s.isNotTerminal()) {
            val a = s.actions.rand(`π`(s))
            val (s_next, reward, _) = a.sample()
            z *= `γ` * `λ`
            z += vFunc.gradient(s)
            val `δ` = reward + `γ` * vFunc(s_next) - vFunc(s)
            vFunc.w += `α` * `δ` * z
            s = s_next
        }
        episodeListener(episode)
    }
}