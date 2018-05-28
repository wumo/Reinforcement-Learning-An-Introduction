@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.problem

import javafx.application.Application
import lab.mars.rl.algo.policy_gradient.`Actor-Critic with Eligibility Traces (episodic)`
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.util.matrix.SparseMatrix
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.MountainCarUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test Mountain Car with Actor-Critic` {
  val numTilings = 8
  val positionScale = numTilings / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
  val velocityScale = numTilings / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
  
  fun func(): LinearFunc<tuple2<DoubleArray, IntArray>> {
    val feature = SuttonTileCoding(511, numTilings) { (s, a) ->
      s as MountainCar.CarState
      a as DefaultAction<Int, MountainCar.CarState>
      tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity),
             intArrayOf(a.value))
    }
    return LinearFunc(feature)
  }
  
  @Test
  fun `Mountain Car UI`() {
    val prob = MountainCar.make()
    
    val policyFeature = SuttonTileCoding(511, numTilings) { (s, a) ->
      s as MountainCar.CarState
      a as DefaultAction<Int, MountainCar.CarState>
      tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity),
             intArrayOf(a.value))
    }
    val h = LinearFunc(policyFeature)
    val emptyIntArray = IntArray(0)
    val valueFeature = SuttonTileCoding(511, numTilings) { (s) ->
      s as MountainCar.CarState
      tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), emptyIntArray)
    }
    val v = LinearFunc(valueFeature)
    
    val episodes = intArrayOf(1, 12, 104, 1000, 9000)
    val latch = CountDownLatch(1)
    thread {
      latch.await()
      prob.`Actor-Critic with Eligibility Traces (episodic)`(
          h = h, α_θ = 2e-9 / numTilings, λ_θ = 0.96,
          v = v, α_w = 0.6 / numTilings, λ_w = 0.96,
          episodes = 9000,
          z_maker = { m, n -> SparseMatrix(m, n) },
          stepListener = step@{ episode, step, s, a ->
            if (episode !in episodes) return@step
            MountainCarUI.render(episode, step, s as MountainCar.CarState, a as DefaultAction<Int, MountainCar.CarState>)
          })
    }
    MountainCarUI.after = { latch.countDown() }
    Application.launch(MountainCarUI::class.java)
  }
}