package lab.mars.rl.algo.mc

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Monte Carlo Off-policy prediction` {
  @Test
  fun `Blackjack`() {
    val (prob, PI) = Blackjack.make()
    val algo = MonteCarlo(prob, PI)
    algo.episodes = 500000
    val V = algo.`Off-policy MC prediction`()
    printBlackjack(prob, PI, V)
  }
}