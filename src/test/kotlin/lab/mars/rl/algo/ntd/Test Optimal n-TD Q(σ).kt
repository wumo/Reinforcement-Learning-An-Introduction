package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.average_α
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal n-TD Q(σ)` {
  @Test
  fun `Blackjack σ=0`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.`N-step off-policy n-step Q(σ)`(
        n = Int.MAX_VALUE,
        σ = { 0 },//same as treebackup
        ε = 0.1,
        α = average_α(prob),
        episodes = 1000000)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `Blackjack σ=1`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.`N-step off-policy n-step Q(σ)`(
        n = Int.MAX_VALUE,
        σ = { 1 },//like off-policy sarsa
        ε = 0.1,
        α = average_α(prob),
        episodes = 1000000)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `Blackjack σ=%2`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.`N-step off-policy n-step Q(σ)`(
        n = Int.MAX_VALUE,
        σ = { it % 2 },
        ε = 0.1,
        α = average_α(prob),
        episodes = 1000000)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `Blackjack σ=random`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.`N-step off-policy n-step Q(σ)`(
        n = Int.MAX_VALUE,
        σ = { Rand().nextInt(2) },
        ε = 0.1,
        α = average_α(prob),
        episodes = 1000000)
    printBlackjack(prob, π, V)
  }
}