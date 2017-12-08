package lab.mars.rl.algo.mc

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Monte Carlo Off-policy prediction` {
  @Test
  fun `Blackjack`() {
    val (prob, π) = Blackjack.make()
    val V = prob.`Off-policy MC prediction`(π, 500_000)
    printBlackjack(prob, π, V)
  }
}