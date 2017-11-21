package lab.mars.rl.algo.func_approx

import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.util.debug
import lab.mars.rl.util.matrix.times

fun FunctionApprox.`Semi-gradient TD(0)`(v: ValueFunction) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        while (s.isNotTerminal()) {
            val a = s.actions.rand(π(s))
            val (s_next, reward, _) = a.sample()
            v.w += α * (reward + γ * v(s_next) - v(s)) * v.`▽`(s)
            s = s_next
        }
        episodeListener(episode)
    }
}