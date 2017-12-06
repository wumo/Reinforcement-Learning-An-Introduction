package lab.mars.rl.algo.td

import lab.mars.rl.problem.*
import lab.mars.rl.util.format
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Prediction Tabular TD(0)` {
  @Test
  fun `Blackjack`() {
    val (prob, PI) = Blackjack.make()
    val algo = TemporalDifference(prob, PI)
    algo.episodes = 500000
    val V = algo.`Tabular TD(0)`()
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `RandomWalk`() {
    val (prob, PI) = RandomWalk.make()
    val algo = TemporalDifference(prob, PI)
    algo.episodes = 1000
    val V = algo.`Tabular TD(0)`()
    prob.apply {
      for (s in states) {
        println("${V[s].format(2)} ")
      }
    }
  }

  @Test
  fun `1000-state RandomWalk`() {
    val (prob, PI) = `1000-state RandomWalk`.make()
    val algo = TemporalDifference(prob, PI)
    algo.episodes = 10000
    val V = algo.`Tabular TD(0)`()
    prob.apply {
      for (s in states) {
        println("${V[s].format(2)} ")
      }
    }
  }
}