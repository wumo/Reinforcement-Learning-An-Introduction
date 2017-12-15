package lab.mars.rl.algo.eligibility_trace.prediction

import lab.mars.rl.model.*
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.Σ
import lab.mars.rl.util.matrix.times
import org.apache.commons.math3.util.FastMath.*

fun <E> MDP.`Off-line λ-return`(
    V: ApproximateFunction<E>,
    π: Policy,
    λ: Double,
    α: Double,
    episodes: Int,
    episodeListener: (Int, Int) -> Unit = { _, _ -> }) {
  val R = newBuf<Double>()
  val S = newBuf<State>()
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var s = started()
    S.clear();S.append(s)
    R.clear();R.append(0.0)
    var T = 0
    while (s.isNotTerminal) {
      val a = π(s)
      val (s_next, reward) = a.sample()
      S.append(s_next)
      R.append(reward)
      s = s_next
      T++
    }
    
    fun Gt(t: Int, n: Int)
        = Σ(1..n) { pow(γ, it - 1) * R[t + it] } +
          (if (t + n < T) pow(γ, n) * V(S[t + n]) else 0.0)
    
    for (t in 0 until T) {
      val Gtλ = (1 - λ) * Σ(1..T - t - 1) { pow(λ, it - 1) * Gt(t, it) } +
                pow(λ, T - t - 1) * Gt(t, T - t)
      V.w += α * (Gtλ - V(S[t])) * V.`∇`(S[t])
    }
    episodeListener(episode, T)
  }
}