package lab.mars.rl.algo.func_approx.off_policy

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.times

fun FunctionApprox.`Semi-gradient off-policy TD(0) episodic`(v: ValueFunction, b: Policy) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        while (s.isNotTerminal()) {
            val a = b(s)
            val (s_next, reward) = a.sample()
            val ρ = π[s, a] / b[s, a]
            val δ = reward + γ * v(s_next) - v(s)
            v.w += α * ρ * δ * v.`▽`(s)
            s = s_next
        }
        episodeListener(episode)
    }
}

fun FunctionApprox.`Semi-gradient off-policy TD(0) continuing`(v: ValueFunction, b: Policy, β: Double) {
    var average_reward = 0.0
    var s = started.rand()
    while (true) {
        val a = b(s)
        val (s_next, reward) = a.sample()
        val ρ = π[s, a] / b[s, a]
        val δ = reward - average_reward + v(s_next) - v(s)
        v.w += α * ρ * δ * v.`▽`(s)
        average_reward += β * δ
        s = s_next
    }
}
