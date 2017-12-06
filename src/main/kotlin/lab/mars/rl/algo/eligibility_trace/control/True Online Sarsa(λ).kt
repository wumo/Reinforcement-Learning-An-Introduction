package lab.mars.rl.algo.eligibility_trace.control

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.*

fun <E> MDP.`True Online Sarsa(λ)`(
    Qfunc: LinearFunc<E>,
    π: Policy,
    λ: Double,
    α: Double,
    episodes: Int,
    episodeListener: (Int, Int) -> Unit = { _, _ -> }) {
  val X = Qfunc.x
  val w = Qfunc.w
  val d = w.size
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    val s = started()
    var a = π(s)
    var x = X(s, a)
    var z = Matrix.column(d)
    var Q_old = 0.0
    while (true) {
      val (s_next, reward) = a.sample()
      val Q = w.T * x
      var δ = reward - Q
      z = γ * λ * z + (1.0 - α * γ * λ * z.T * x) * x
      if (s_next.isNotTerminal) {
        val a_next = π(s_next)
        val `x'` = X(s_next, a_next)
        val `Q'` = (w.T * `x'`).toScalar
        δ += γ * `Q'`
        w += α * (δ + Q - Q_old) * z - α * (Q - Q_old) * x
        Q_old = `Q'`
        x = `x'`
        a = a_next
      } else {
        w += α * (δ + Q - Q_old) * z - α * (Q - Q_old) * x
        break
      }
      step++
    }
    episodeListener(episode, step)
  }
}