package lab.mars.rl.algo.td

import lab.mars.rl.algo.average_alpha
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.*
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal TD Expected sarsa` {
  @Test
  fun `Blackjack constant alpha`() {
    val (prob, policy) = Blackjack.make()
    val algo = TemporalDifference(prob, policy)
    algo.episodes = 1000000
    val (PI, V, _) = algo.expectedSarsa()
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Blackjack average alpha`() {
    val (prob, policy) = Blackjack.make()
    val algo = TemporalDifference(prob, policy)
    algo.episodes = 1000000
    val (PI, V, _) = algo.expectedSarsa(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Cliff Walking TD Expected Sarsa`() {
    val prob = CliffWalking.make()
    val algo = TemporalDifference(prob)
    algo.α = 0.5
    val (PI, _, _) = algo.expectedSarsa()
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
    println("\nreturn=$sum")//optimal=-12
  }
}