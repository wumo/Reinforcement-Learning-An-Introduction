package lab.mars.rl.algo.func_approx.on_policy_control

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.model.ActionValueApproxFunction
import lab.mars.rl.util.matrix.times

fun FunctionApprox.`Differential semi-gradient Sarsa`(qFunc: ActionValueApproxFunction, β: Double) {
    var average_reward = 0.0
    var s = started()
    π.`ε-greedy update`(s, qFunc)
    var a = π(s)
    while (true) {
        val (s_next, reward) = a.sample()
        π.`ε-greedy update`(s_next, qFunc)
        val a_next =π(s_next)
        val δ = reward - average_reward + qFunc(s_next, a_next) - qFunc(s, a)
        average_reward += β * δ
        qFunc.w += α * δ * qFunc.`▽`(s, a)
        s = s_next
        a = a_next
    }
}