package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.average_α
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal RandomSampleOneStepTabularQLearning` {
  @Test
  fun `Blackjack`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.RandomSampleOneStepTabularQLearning(
        ε = 0.1,
        α = average_α(prob),
        episodes = 1000000)
    printBlackjack(prob, π, V)
  }
  
}