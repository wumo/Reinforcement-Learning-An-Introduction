package lab.mars.rl.algo.eligibility_trace

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.Matrix
import lab.mars.rl.util.matrix.times

fun <E> FunctionApprox.`Semi-gradient TD(λ) prediction`(vFunc: LinearFunc<E>, λ: Double) {
  var z = Matrix.column(vFunc.w.size)
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    while (s.isNotTerminal()) {
      step++
      val a = π(s)
      val (s_next, reward) = a.sample()
      z = γ * λ * z + vFunc.`▽`(s)
      val δ = reward + γ * vFunc(s_next) - vFunc(s)
      vFunc.w += α * δ * z
      s = s_next
    }
    episodeListener(episode, step)
  }
}