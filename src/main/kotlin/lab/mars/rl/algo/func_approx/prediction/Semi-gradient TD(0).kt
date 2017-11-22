package lab.mars.rl.algo.func_approx.prediction

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.*

fun FunctionApprox.`Semi-gradient TD(0)`(v: ValueFunction) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        while (s.isNotTerminal()) {
            val a = π(s)
            val (s_next, reward) = a.sample()
            v.w += α * (reward + γ * v(s_next) - v(s)) * v.`▽`(s)
            s = s_next
        }
        episodeListener(episode)
    }
}