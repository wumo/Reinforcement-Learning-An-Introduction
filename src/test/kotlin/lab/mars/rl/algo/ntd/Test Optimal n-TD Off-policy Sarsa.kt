package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.average_alpha
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.*
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal n-TD Off-policy Sarsa` {

  @Test
  fun `Blackjack constant alpha`() {
    val (prob, policy) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
    algo.α = 0.1
    algo.episodes = 1000000
    val (PI, V, _) = algo.`off-policy sarsa`()
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Blackjack average alpha`() {
    val (prob, policy) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
    algo.episodes = 1000000
    val (PI, V, _) = algo.`off-policy sarsa`(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Cliff Walking`() {
    val prob = CliffWalking.make()
    val algo = NStepTemporalDifference(prob, 10)
    algo.α = 0.5
    val (PI, _, _) = algo.`off-policy sarsa`()
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