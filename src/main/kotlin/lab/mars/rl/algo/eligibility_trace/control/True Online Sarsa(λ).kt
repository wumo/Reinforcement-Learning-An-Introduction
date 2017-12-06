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
  val d = X.numOfComponents
  var z = Matrix.column(d)
  var Q_old = 0.0
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var a = π(s)
    var x = X(s, a)
    while (s.isNotTerminal) {
      val (s_next, reward) = a.sample()
      val a_next = π(s_next)
      val `x'` = X(s_next, a_next)
      val Q = (w.T * x).asScalar()
      val `Q'` = (w.T * `x'`).asScalar()
      val δ = reward + γ * `Q'` - Q
      z = γ * λ * z + (1.0 - α * γ * λ * z.T * x) * x
      w += α * (δ + Q - Q_old) * z - α * (Q - Q_old) * x
      Q_old = `Q'`
      x = `x'`
      a = a_next
      s = s_next
      step++
    }
    episodeListener(episode, step)
  }
}