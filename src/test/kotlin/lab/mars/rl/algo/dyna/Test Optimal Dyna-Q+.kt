package lab.mars.rl.algo.dyna

import javafx.application.Application
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.DynaMaze
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.ui.GridWorldUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test Optimal Dyna-Q+` {
  @Test
  fun `Dyna Maze`() {
    val prob = DynaMaze.make()
    val latch = CountDownLatch(1)
    
    thread {
      latch.await()
      val (π) = prob.`Dyna-Q+`(
          n = 10,
          α = { _, _ -> 0.1 },
          ε = 0.1,
          κ = 1e-4,
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
}