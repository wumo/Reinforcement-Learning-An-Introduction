package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.average_alpha
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.problem.WindyGridworld
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal n-TD Sarsa` {
  @Test
  fun `Blackjack constant alpha`() {
    val (prob, `π`) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, `π`)
    algo.α = 0.1
    algo.episodes = 1000000
    val (PI, V, _) = algo.sarsa()
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Blackjack average alpha`() {
    val (prob, `π`) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, `π`)
    algo.episodes = 1000000
    val (PI, V, _) = algo.sarsa(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `WindyGridworld`() {
    val prob = WindyGridworld.make()
    val algo = NStepTemporalDifference(prob, 10)
    algo.α = 0.1
    algo.episodes = 1000000
    val (PI, _, _) = algo.sarsa(average_alpha(prob))
    var s = prob.started()
    var sum = 0.0
    print(s)
    while (s.isNotTerminal) {
      val a = argmax(s.actions) { PI[s, it] }
      val possible = a.sample()
      s = possible.next
      sum += possible.reward
      print("${WindyGridworld.desc_move[a[0]]}$s")
    }
    println("\nreturn=$sum")//optimal=-14
  }
}