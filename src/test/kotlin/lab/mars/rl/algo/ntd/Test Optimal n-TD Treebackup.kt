package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.average_alpha
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal n-TD Treebackup` {
  @Test
  fun `Blackjack constant alpha`() {
    val (prob, policy) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, 4, policy)
    algo.α = 0.1
    algo.episodes = 1000000
    val (PI, V, _) = algo.treebackup()
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Blackjack average alpha`() {
    val (prob, policy) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
    algo.episodes = 1000000
    val (PI, V, _) = algo.treebackup(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }

}