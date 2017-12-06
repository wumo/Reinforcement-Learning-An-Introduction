package lab.mars.rl.algo.dyna

import javafx.application.Application
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.*
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.ui.GridWorldUI
import lab.mars.rl.util.ui.RodManeuveringUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test Optimal Prioritized Sweeping` {
  @Test
  fun `Dyna Maze UI`() {
    val prob = DynaMaze.make()
    val algo = PrioritizedSweeping(prob)
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

  @Test
  fun `WindyGridworld`() {
    val prob = WindyGridworld.make()
    val algo = PrioritizedSweeping(prob)
    algo.α = 0.5
    algo.episodes = 1000
    val (PI, _, _) = algo.optimal()
    var s = prob.started()
    var sum = 0.0
    print(s)
    while (s.isNotTerminal) {
      val a = argmax(s.actions) { PI[s, it] }
      val possible = a.sample()
      s = possible.next
      sum += possible.reward
      print("${WindyGridworld.desc_move[a[0]]}$s")
    }
    println("\nreturn=$sum")//optimal=-14
  }

  @Test
  fun `Cliff Walking`() {
    val prob = CliffWalking.make()
    val algo = PrioritizedSweeping(prob)
    algo.α = 0.5
    algo.episodes = 1000
    val (PI, _, _) = algo.optimal()
    var s = prob.started()
    var sum = 0.0
    print(s)
    while (s.isNotTerminal) {
      val a = argmax(s.actions) { PI[s, it] }
      val possible = a.sample()
      s = possible.next
      sum += possible.reward
      print("${WindyGridworld.desc_move[a[0]]}$s")
    }
    println("\nreturn=$sum")//optimal=-12
  }

  @Test
  fun `Rod Maneuvering UI`() {
    val prob = RodManeuvering.make()
    val algo = PrioritizedSweeping(prob)
    algo.episodes = 1000
    algo.n = 10
    val latch = CountDownLatch(1)

    thread {
      latch.await()
      algo.stepListener = { V, s ->
        RodManeuveringUI.render(V, s)
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
        print("$a$s")
      }
      println("\nsteps=$count")//optimal=39
    }
    RodManeuveringUI.after = { latch.countDown() }
    Application.launch(RodManeuveringUI::class.java)
  }

}