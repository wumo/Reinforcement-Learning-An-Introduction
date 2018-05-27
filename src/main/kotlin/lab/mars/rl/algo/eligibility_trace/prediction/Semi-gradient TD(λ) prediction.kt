package lab.mars.rl.algo.eligibility_trace.prediction

import lab.mars.rl.model.*
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.Matrix
import lab.mars.rl.util.matrix.MatrixSpec
import lab.mars.rl.util.matrix.SparseMatrix
import lab.mars.rl.util.matrix.times

fun <E> MDP.`Semi-gradient TD(λ) prediction`(
    V: ApproximateFunction<E>,
    π: Policy,
    λ: Double,
    α: Double,
    episodes: Int,
    z_maker: (Int, Int) -> MatrixSpec = { m, n -> Matrix(m, n) },
    episodeListener: (Int, Int) -> Unit = { _, _ -> }) {
  val w = V.w
  val d = w.size
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    val z = z_maker(d, 1)
    while (s.isNotTerminal) {
      val a = π(s)
      val (s_next, reward) = a.sample()
      z `=` γ * λ * z + V.`∇`(s)
      val δ = reward + γ * (if (s_next.isTerminal) 0.0 else V(s_next)) - V(s)
      V.w += α * δ * z
      s = s_next
      step++
    }
    episodeListener(episode, step)
  }
}