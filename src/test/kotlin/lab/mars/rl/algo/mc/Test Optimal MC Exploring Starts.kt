package lab.mars.rl.algo.mc

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal MC Exploring Starts` {
  @Test
  fun `Blackjack`() {
    val (prob, π) = Blackjack.make()
    val (PI, V) = prob.`Monte Carlo Exploring Starts`(π, 1000000)
    printBlackjack(prob, PI, V)
  }
}