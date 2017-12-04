package lab.mars.rl.algo.ntd

import lab.mars.rl.problem.Blackjack
import lab.mars.rl.problem.RandomWalk
import lab.mars.rl.util.format
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Prediction n-TD` {
  @Test
  fun `Blackjack`() {
    val (prob, `π`) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, 102400, `π`)
    algo.episodes = 500000
    val V = algo.prediction()
    printBlackjack(prob, `π`, V)
  }

  @Test
  fun `RandomWalk`() {
    val (prob, PI) = RandomWalk.make()
    val algo = NStepTemporalDifference(prob, 8, PI)
    algo.episodes = 1000
    val V = algo.prediction()
    prob.apply {
      for (s in states) {
        println("${V[s].format(2)} ")
      }
    }
  }
}