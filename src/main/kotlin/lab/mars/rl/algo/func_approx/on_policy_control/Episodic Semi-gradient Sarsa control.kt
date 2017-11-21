package lab.mars.rl.algo.func_approx.on_policy_control

import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.ActionValueApproxFunction
import lab.mars.rl.util.debug
import lab.mars.rl.util.matrix.times

fun FunctionApprox.`Episodic semi-gradient Sarsa control`(qFunc: ActionValueApproxFunction) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        `ε-greedy`(s, qFunc, π, ε)
        var a = s.actions.rand(π(s))
        while (true) {
            val (s_next, reward, _) = a.sample()
            if (s_next.isNotTerminal()) {
                `ε-greedy`(s_next, qFunc, π, ε)
                val a_next = s_next.actions.rand(π(s_next))
                qFunc.w += α * (reward + γ * qFunc(s_next, a_next) - qFunc(s, a)) * qFunc.`▽`(s, a)
                s = s_next
                a = a_next
            } else {
                qFunc.w += α * (reward - qFunc(s, a)) * qFunc.`▽`(s, a)
                break
            }
        }
        episodeListener(episode)
    }
}