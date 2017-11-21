package lab.mars.rl.algo.func_approx

import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.model.ActionValueApproxFunction
import lab.mars.rl.util.matrix.times

fun FunctionApprox.`Differential semi-gradient Sarsa`(qFunc: ActionValueApproxFunction, β: Double) {
    var average_reward = 0.0
    var s = started.rand()
    `ε-greedy`(s, qFunc, π, ε)
    var a = s.actions.rand(π(s))
    while (true) {
        val (s_next, reward, _) = a.sample()
        `ε-greedy`(s_next, qFunc, π, ε)
        val a_next = s_next.actions.rand(π(s_next))
        val δ = reward - average_reward + qFunc(s_next, a_next) - qFunc(s, a)
        average_reward += β * δ
        qFunc.w += α * δ * qFunc.`▽`(s, a)
        s = s_next
        a = a_next
    }
}