package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.average_alpha
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal RandomSampleOneStepTabularQLearning` {
  @Test
  fun `Blackjack`() {
    val (prob, _) = Blackjack.make()
    val algo = RandomSampleOneStepTabularQLearning(prob)
    algo.episodes = 1000000
    val (PI, V, _) = algo.optimal(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }

}