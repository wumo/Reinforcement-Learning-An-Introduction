package lab.mars.rl.algo.func_approx.off_policy

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.times

fun <E> FunctionApprox.`Semi-gradient off-policy TD(0) episodic`(v: ApproximateFunction<E>, trans: (State) -> E, b: Policy) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var step = 0
        var s = started()
        while (s.isNotTerminal()) {
            step++
            val a = b(s)
            val (s_next, reward) = a.sample()
            val ρ = π[s, a] / b[s, a]
            val δ = reward + γ * v(trans(s_next)) - v(trans(s))
            v.w += α * ρ * δ * v.`▽`(trans(s))
            s = s_next
        }
        episodeListener(episode, step)
    }
}

fun <E> FunctionApprox.`Semi-gradient off-policy TD(0) continuing`(v: ApproximateFunction<E>, trans: (State) -> E, b: Policy, β: Double) {
    var average_reward = 0.0
    var s = started()
    while (true) {
        val a = b(s)
        val (s_next, reward) = a.sample()
        val ρ = π[s, a] / b[s, a]
        val δ = reward - average_reward + v(trans(s_next)) - v(trans(s))
        v.w += α * ρ * δ * v.`▽`(trans(s))
        average_reward += β * δ
        s = s_next
    }
}
