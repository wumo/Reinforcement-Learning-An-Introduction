package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.average_alpha
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal n-TD Q(σ)` {
  @Test
  fun `Blackjack σ=0`() {
    val (prob, policy) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
    algo.σ = { 0 }//相当于treebackup
    algo.episodes = 1000000
    val (PI, V, _) = algo.`off-policy n-step Q(σ)`(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Blackjack σ=1`() {
    val (prob, policy) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
    algo.σ = { 1 }//相当于off-policy sarsa?但结果似乎不像
    algo.episodes = 1000000
    val (PI, V, _) = algo.`off-policy n-step Q(σ)`(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Blackjack σ=%2`() {
    val (prob, policy) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
    algo.σ = { it % 2 }
    algo.episodes = 1000000
    val (PI, V, _) = algo.`off-policy n-step Q(σ)`(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Blackjack σ=random`() {
    val (prob, policy) = Blackjack.make()
    val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
    algo.σ = { Rand().nextInt(2) }
    algo.episodes = 1000000
    val (PI, V, _) = algo.`off-policy n-step Q(σ)`(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }
}