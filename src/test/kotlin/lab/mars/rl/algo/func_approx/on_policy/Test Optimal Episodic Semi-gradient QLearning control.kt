@file:Suppress("UNCHECKED_CAST", "NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.on_policy

import javafx.application.Application
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.EpsilonGreedyFunctionPolicy
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.problem.MountainCar.CarState
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.MountainCarUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test Optimal Episodic Semi-gradient QLearning control` {
  
  @Test
  fun `Mountain Car UI`() {
    val prob = MountainCar.make()
    val feature = SuttonTileCoding(511, 8, doubleArrayOf(8 / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN),
                                                         8 / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN))) { (s, a) ->
      s as CarState
      a as DefaultAction<Int, CarState>
      tuple2(doubleArrayOf(s.position, s.velocity), intArrayOf(a.value))
    }
    val func = LinearFunc(feature)
    
    val episodes = intArrayOf(1, 12, 104, 1000, 9000)
    val latch = CountDownLatch(1)
    thread {
      latch.await()
      prob.`Episodic semi-gradient QLearning control`(
          Q = func,
          π = EpsilonGreedyFunctionPolicy(func, 0.0),
          α = 0.3 / 8,
          episodes = 9000,
          stepListener = step@{ episode, step, s, a ->
            if (episode !in episodes) return@step
            MountainCarUI.render(episode, step, s as CarState, a as DefaultAction<Int, CarState>)
          })
    }
    MountainCarUI.after = { latch.countDown() }
    Application.launch(MountainCarUI::class.java)
  }
  
}