package lab.mars.rl.algo.func_approx.prediction

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.times

fun <E> FunctionApprox.`Semi-gradient TD(0)`(v: ApproximateFunction<E>, trans: (State) -> E) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var step=0
        var s = started()
        while (s.isNotTerminal()) {
            step++
            val a = π(s)
            val (s_next, reward) = a.sample()
            v.w += α * (reward + γ * (if (s_next.isTerminal()) 0.0 else v(trans(s_next))) - v(trans(s))) * v.`▽`(trans(s))
            s = s_next
        }
        episodeListener(episode,step)
    }
}