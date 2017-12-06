package lab.mars.rl.algo.td

import lab.mars.rl.algo.average_α
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.*
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.printBlackjack
import org.junit.Test

class `Test Optimal TD Q-Learning` {
  @Test
  fun `Blackjack constant alpha`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.QLearning(ε = 0.1, α = { _, _ -> 0.5 }, episodes = 100000)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `Blackjack average alpha`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.QLearning(ε = 0.1, α = average_α(prob), episodes = 1000000)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `WindyGridworld`() {
    val prob = WindyGridworld.make()
    val (π) = prob.QLearning(ε = 0.1, α = { _, _ -> 0.5 }, episodes = 1000)
    var s = prob.started()
    var sum = 0.0
    print(s)
    while (s.isNotTerminal) {
      val a = argmax(s.actions) { π[s, it] }
      val possible = a.sample()
      s = possible.next
      sum += possible.reward
      print("${WindyGridworld.desc_move[a[0]]}$s")
    }
    println("\nreturn=$sum")//optimal=-14
  }
  
  @Test
  fun `WindyGridworld King's Move`() {
    val prob = WindyGridworld.make(true)
    val (π) = prob.QLearning(ε = 0.1, α = { _, _ -> 0.5 }, episodes = 1000)
    var s = prob.started()
    var sum = 0.0
    print(s)
    while (s.isNotTerminal) {
      val a = argmax(s.actions) { π[s, it] }
      val possible = a.sample()
      s = possible.next
      sum += possible.reward
      print("${WindyGridworld.desc_king_move[a[0]]}$s")
    }
    println("\nreturn=$sum")//optimal=-6
  }
  
  @Test
  fun `Cliff Walking`() {
    val prob = CliffWalking.make()
    val (π) = prob.QLearning(ε = 0.1, α = { _, _ -> 0.5 }, episodes = 1000)
    var s = prob.started()
    var sum = 0.0
    print(s)
    while (s.isNotTerminal) {
      val a = argmax(s.actions) { π[s, it] }
      val possible = a.sample()
      s = possible.next
      sum += possible.reward
      print("${WindyGridworld.desc_move[a[0]]}$s")
    }
    println("\nreturn=$sum")//optimal=-12
  }
}