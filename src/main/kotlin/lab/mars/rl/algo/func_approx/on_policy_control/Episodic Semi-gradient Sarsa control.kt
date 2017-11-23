package lab.mars.rl.algo.func_approx.on_policy_control

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.debug
import lab.mars.rl.util.matrix.times

fun <E> FunctionApprox.`Episodic semi-gradient Sarsa control`(q: ApproximateFunction<E>, trans: (State, Action<State>) -> E) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started()
        var a = π(s)
        while (true) {
            val (s_next, reward) = a.sample()
            if (s_next.isNotTerminal()) {
                val a_next = π(s_next)
                q.w += α * (reward + γ * q(trans(s_next, a_next)) - q(trans(s, a))) * q.`▽`(trans(s, a))
                s = s_next
                a = a_next
            } else {
                q.w += α * (reward - q(trans(s, a))) * q.`▽`(trans(s, a))
                break
            }
        }
        episodeListener(episode)
    }
}