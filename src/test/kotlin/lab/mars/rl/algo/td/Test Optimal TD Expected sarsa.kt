package lab.mars.rl.algo.td

import lab.mars.rl.algo.average_α
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.*
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal TD Expected sarsa` {
  @Test
  fun `Blackjack constant alpha`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.expectedSarsa(ε = 0.1, α = { _, _ -> 0.5 }, episodes = 1000_000)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `Blackjack average alpha`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.expectedSarsa(ε = 0.1, α = average_α(prob), episodes = 1000_000)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `Cliff Walking TD Expected Sarsa`() {
    val prob = CliffWalking.make()
    val (PI) = prob.expectedSarsa(ε = 0.1, α = { _, _ -> 0.5 }, episodes = 1000_000)
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