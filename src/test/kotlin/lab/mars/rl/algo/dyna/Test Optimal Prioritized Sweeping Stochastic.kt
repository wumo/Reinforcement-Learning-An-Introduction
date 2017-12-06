package lab.mars.rl.algo.dyna

import javafx.application.Application
import lab.mars.rl.algo.average_alpha
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.problem.DynaMaze
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.printBlackjack
import lab.mars.rl.util.ui.GridWorldUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test Optimal Prioritized Sweeping Stochastic` {
  @Test
  fun `Blackjack`() {
    val (prob, _) = Blackjack.make()
    val algo = PrioritizedSweepingStochasticEnv(prob)
    algo.episodes = 100000
    algo.n = 10
    val (PI, V, _) = algo.optimal(average_alpha(prob))
    printBlackjack(prob, PI, V)
  }

  @Test
  fun `Dyna Maze UI`() {
    val prob = DynaMaze.make()
    val algo = PrioritizedSweepingStochasticEnv(prob)
    algo.episodes = 1000
    algo.n = 10
    val latch = CountDownLatch(1)

    thread {
      latch.await()
      algo.stepListener = { V, s ->
        GridWorldUI.render(V, s)
      }
      val (PI, _, _) = algo.optimal()
      var s = prob.started()
      var count = 0
      print(s)
      while (s.isNotTerminal) {
        val a = argmax(s.actions) { PI[s, it] }
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
}