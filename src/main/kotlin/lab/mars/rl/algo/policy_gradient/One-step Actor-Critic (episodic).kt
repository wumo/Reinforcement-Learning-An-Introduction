package lab.mars.rl.algo.policy_gradient

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.rand
import lab.mars.rl.util.matrix.times
import lab.mars.rl.util.matrix.Σ

fun <E> FunctionApprox.`One-step Actor-Critic (episodic)`(
  π: ApproximateFunction<E>, α_θ: Double,
  v: ApproximateFunction<E>, α_w: Double) {
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var γ_t = 1.0
    while (s.isNotTerminal()) {
      step++
      val a = rand(s.actions) { π(s, it) }
      val (s_next, reward) = a.sample()
      val δ = reward + γ * if (s_next.isTerminal()) 0.0 else v(s_next) - v(s)
      v.w += α_w * γ_t * δ * v.`▽`(s)
      val `▽` = if (π is LinearFunc)
        π.x(s, a) - Σ(s.actions) { π(s, it) * π.x(s, it) }
      else
        π.`▽`(s, a) / π(s, a)
      π.w += α_θ * γ_t * δ * `▽`
      γ_t *= γ
      s = s_next
    }
  }
}