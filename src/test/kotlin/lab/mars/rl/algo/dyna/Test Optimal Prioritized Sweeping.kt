package lab.mars.rl.algo.dyna

import javafx.application.Application
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.CliffWalking
import lab.mars.rl.problem.DynaMaze
import lab.mars.rl.problem.RodManeuvering
import lab.mars.rl.problem.WindyGridworld
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
    val latch = CountDownLatch(1)
    
    thread {
      latch.await()
      val (π) = prob.PrioritizedSweeping(
          n = 10,
          θ = 0.0,
          ε = 0.1,
          α = { _, _ -> 0.1 },
          episodes = 1000,
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
  fun `WindyGridworld`() {
    val prob = WindyGridworld.make()
    val (π) = prob.PrioritizedSweeping(
        n = 10,
        θ = 0.0,
        ε = 0.1,
        α = { _, _ -> 0.5 },
        episodes = 1000)
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
  fun `Cliff Walking`() {
    val prob = CliffWalking.make()
    val (π) = prob.PrioritizedSweeping(
        n = 10,
        θ = 0.0,
        ε = 0.1,
        α = { _, _ -> 0.5 },
        episodes = 1000)
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
  
  @Test
  fun `Rod Maneuvering UI`() {
    val prob = RodManeuvering.make()
    val latch = CountDownLatch(1)
    
    thread {
      latch.await()
      val (π) = prob.PrioritizedSweeping(
          n = 10,
          θ = 0.0,
          ε = 0.1,
          α = { _, _ -> 0.1 },
          episodes = 1000,
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