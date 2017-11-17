package lab.mars.rl.algo.func_approx

import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.util.debug

fun FunctionApprox.`Semi-gradient TD(0)`(v: ValueFunction) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        while (s.isNotTerminal()) {
            val a = s.actions.rand(π(s))
            val (s_next, reward, _) = a.sample()
            v.update(s, α * (reward + γ * v(s_next) - v(s)))
            s = s_next
        }
        episodeListener(episode)
    }
}