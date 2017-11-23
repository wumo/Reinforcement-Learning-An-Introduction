package lab.mars.rl.algo.func_approx.off_policy

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.Σ
import lab.mars.rl.util.matrix.times

fun <E> FunctionApprox.`Semi-gradient Expected Sarsa`(q: ApproximateFunction<E>, trans: (State, Action<State>) -> E) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started()
        while (s.isNotTerminal()) {
            val a = π(s)
            val (s_next, reward) = a.sample()
            val δ = reward + γ * Σ(s_next.actions) { π[s_next, it] * q(trans(s_next, it)) } - q(trans(s, a))
            q.w += α * δ * q.`▽`(trans(s, a))
            s = s_next
        }
        episodeListener(episode)
    }
}

fun <E> FunctionApprox.`Semi-gradient Expected Sarsa`(q: ApproximateFunction<E>, trans: (State, Action<State>) -> E, β: Double) {
    var average_reward = 0.0
    var s = started()
    while (true) {
        val a = π(s)
        val (s_next, reward) = a.sample()
        val δ = reward - average_reward + Σ(s_next.actions) { π[s_next, it] * q(trans(s_next, it)) } - q(trans(s, a))
        q.w += α * δ * q.`▽`(trans(s, a))
        average_reward += β * δ
        s = s_next
    }
}