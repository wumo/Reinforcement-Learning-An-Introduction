package lab.mars.rl.algo.mc

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal MC Off-policy` {
  @Test
  fun `Blackjack`() {
    val (prob, policy1) = Blackjack.make()
    val algo = MonteCarlo(prob, policy1)
    algo.episodes = 1000000
    val (PI, V, _) = algo.`Off-policy MC Optimal`()
    printBlackjack(prob, PI, V)
  }
}