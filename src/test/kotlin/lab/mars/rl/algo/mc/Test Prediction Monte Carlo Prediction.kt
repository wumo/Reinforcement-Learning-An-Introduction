package lab.mars.rl.algo.mc

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.problem.RandomWalk
import lab.mars.rl.util.format
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Prediction Monte Carlo Prediction` {
  @Test
  fun `Blackjack`() {
    val (prob, π) = Blackjack.make()
    val V = prob.`Monte Carlo Prediction`(π, 500000)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `RandomWalk`() {
    val (prob, π) = RandomWalk.make()
    val V = prob.`Monte Carlo Prediction`(π, 1000)
    prob.apply {
      for (s in states) {
        println("${V[s].format(2)} ")
      }
    }
  }
}