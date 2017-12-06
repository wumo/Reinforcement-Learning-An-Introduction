package lab.mars.rl.algo.dyna

import javafx.application.Application
import lab.mars.rl.algo.average_α
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.*
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.printBlackjack
import lab.mars.rl.util.ui.GridWorldUI
import lab.mars.rl.util.ui.RodManeuveringUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test Optimal Dyna-Q on-policy` {
  @Test
  fun `Blackjack`() {
    val (prob) = Blackjack.make()
    val (π, V) = prob.`Dyna-Q-OnPolicy`(
        α = average_α(prob),
        episodes = 1000000,
        n = 10)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `Dyna Maze UI`() {
    val prob = DynaMaze.make()
    val latch = CountDownLatch(1)
    
    thread {
      latch.await()
      val (π) = prob.`Dyna-Q-OnPolicy`(
          α = average_α(prob),
          episodes = 1000,
          n = 20,
          stepListener = { V, s ->
            GridWorldUI.render(V, s)
          })
      var s = prob.started()
      var count = 0
      print(s)
      while (s.isNotTerminal) {
        val a = argmax(s.actions) { π[s, it] }
        val possible = a.sample()
        s = possible.next
        count++
        print("${DynaMaze.desc_move[a[0]]}$s")
      }
      println("\nsteps=$count")//optimal=14
    }
    GridWorldUI.after = { latch.countDown() }
    Application.launch(GridWorldUI::class.java)
  }
  
  @Test
  fun `Rod Maneuvering UI`() {
    val prob = RodManeuvering.make()
    val latch = CountDownLatch(1)
    
    thread {
      latch.await()
      val (π) = prob.`Dyna-Q-OnPolicy`(
          α = average_α(prob),
          episodes = 1000,
          n = 20,
          stepListener = { V, s ->
            RodManeuveringUI.render(V, s)
          })
      var s = prob.started()
      var count = 0
      print(s)
      while (s.isNotTerminal) {
        val a = argmax(s.actions) { π[s, it] }
        val possible = a.sample()
        s = possible.next
        count++
        print("$a$s")
      }
      println("\nsteps=$count")//optimal=39
    }
    RodManeuveringUI.after = { latch.countDown() }
    Application.launch(RodManeuveringUI::class.java)
  }
}