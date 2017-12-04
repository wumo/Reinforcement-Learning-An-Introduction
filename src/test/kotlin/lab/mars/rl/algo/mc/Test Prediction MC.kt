package lab.mars.rl.algo.mc

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.problem.RandomWalk
import lab.mars.rl.util.format
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Prediction MC` {
  @Test
  fun `Blackjack`() {
    val (prob, PI) = Blackjack.make()
    val algo = MonteCarlo(prob, PI)
    algo.episodes = 500000
    val V = algo.prediction()
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `RandomWalk`() {
    val (prob, PI) = RandomWalk.make()
    val algo = MonteCarlo(prob, PI)
    algo.episodes = 1000
    val V = algo.prediction()
    prob.apply {
      for (s in states) {
        println("${V[s].format(2)} ")
      }
    }
  }
}