package lab.mars.rl.algo.mc

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal MC Off-policy` {
  @Test
  fun `Blackjack`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.`Off-policy MC Optimal`(1000_000)
    printBlackjack(prob, π, V)
  }
}