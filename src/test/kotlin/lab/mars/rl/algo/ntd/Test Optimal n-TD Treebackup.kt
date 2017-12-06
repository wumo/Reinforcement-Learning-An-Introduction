package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.average_α
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal n-TD Treebackup` {
  @Test
  fun `Blackjack constant alpha`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.`N-step Treebackup`(
        n = 4, ε = 0.1,
        episodes = 1000000,
        α = { _, _ -> 0.1 })
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `Blackjack average alpha`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.`N-step Treebackup`(
        n = Int.MAX_VALUE, ε = 0.1,
        episodes = 1000000,
        α = average_α(prob))
    printBlackjack(prob, π, V)
  }
  
}