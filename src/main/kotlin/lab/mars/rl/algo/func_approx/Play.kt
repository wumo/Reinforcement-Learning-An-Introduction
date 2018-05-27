package lab.mars.rl.algo.func_approx

import lab.mars.rl.model.*

fun MDP.play(
    π: Policy,
    episodes: Int,
    maxStep: Int = Int.MAX_VALUE,
    episodeListener: (Int, Int, State, Double) -> Unit = { _, _, _, _ -> },
    stepListener: (Int, Int, State, Action<State>) -> Unit = { _, _, _, _ -> }) {
  for (episode in 1..episodes) {
    var s = started()
    var step = 0
    var G = 0.0
    var γn = 1.0
    while (s.isNotTerminal) {
      val a = π(s)
      stepListener(episode, step, s, a)
      val (s_next, reward) = a.sample()
      γn *= γ
      G += γn * reward
      s = s_next
      step++
      if (step >= maxStep)
        break
    }
    episodeListener(episode, step, s, G)
  }
}