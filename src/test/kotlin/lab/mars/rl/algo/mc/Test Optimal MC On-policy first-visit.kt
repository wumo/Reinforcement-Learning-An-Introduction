package lab.mars.rl.algo.mc

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal MC On-policy first-visit` {
  @Test
  fun `Blackjack`() {
    val (prob, policy1) = Blackjack.make()
    val algo = MonteCarlo(prob, policy1)
    algo.episodes = 1000000
    val (PI, V, _) = algo.`On-policy first-visit MC control`()
    printBlackjack(prob, PI, V)
  }
}