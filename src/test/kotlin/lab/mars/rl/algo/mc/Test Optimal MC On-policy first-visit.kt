package lab.mars.rl.algo.mc

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal MC On-policy first-visit` {
  @Test
  fun `Blackjack`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.`On-policy first-visit MC control`(1000000)
    printBlackjack(prob, π, V)
  }
}