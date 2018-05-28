@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.problem

import javafx.application.Application
import lab.mars.rl.algo.func_approx.play
import lab.mars.rl.algo.policy_gradient.`Actor-Critic with Eligibility Traces (episodic)`
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.SoftmaxpPolicy
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.D2DGameUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test FlyPlane Problem with Actor-Critic` {
  @Test
  fun `Fly Plane UI`() {
    val numTilings = 10
    val emptyIntArray = IntArray(0)
    val valueFeature = SuttonTileCoding(1000, numTilings, doubleArrayOf(1 / 100.0, 1 / 100.0, 1 / 10.0, 1 / 10.0)) { (s) ->
      s as FlyPlane.PlaneState
      tuple2(doubleArrayOf(s.loc.x, s.loc.y, s.vel.x, s.vel.y), emptyIntArray)
    }
    val v = LinearFunc(valueFeature)
    val policyFeature = SuttonTileCoding(1000, numTilings, doubleArrayOf(1 / 100.0, 1 / 100.0, 1 / 10.0, 1 / 10.0)) { (s, a) ->
      s as FlyPlane.PlaneState
      a as DefaultAction<Int, FlyPlane.PlaneState>
      tuple2(doubleArrayOf(s.loc.x, s.loc.y, s.vel.x, s.vel.y), intArrayOf(a.value))
    }
    val h = LinearFunc(policyFeature)
    val resolution = 100
    val unit = FlyPlane.fieldWidth / resolution
    val qvalue = Array(resolution) { Array(resolution + 1) { Double.NEGATIVE_INFINITY } }
    var accuG = 0.0
    var wins = 0.0
    var win_step = 0.0
    val episode_round = 100
    val step_round = 10
    val max_episode = 10000
    var episode_base = 0
    var animate = false
    val latch = CountDownLatch(1)
    thread {
      latch.await()
      
      while (true) {
        val prob = FlyPlane.makeRand()
        animate = false
        prob.`Actor-Critic with Eligibility Traces (episodic)`(
            h = h, α_θ = 1e-12, λ_θ = 0.5,
            v = v, α_w = 0.006, λ_w = 0.5,
            episodes = max_episode
        )
        animate = true
        prob.play(
            π = SoftmaxpPolicy(h),
            episodes = 10,
            stepListener = { _, _, s, a ->
              Thread.sleep(Math.floor(1000 / 60.0).toLong())
            }
        )
        episode_base += max_episode
      }
    }
    D2DGameUI.apply {
      canvas_width = FlyPlane.fieldWidth
      canvas_height = FlyPlane.fieldWidth
      width = 1200.0
      height = 800.0
      charts.addAll(D2DGameUI.ChartDescription("average return per $episode_round episodes", "episode", "average return"),
                    D2DGameUI.ChartDescription("win rate per $episode_round episodes", "episode", "win rate"),
                    D2DGameUI.ChartDescription("average win step per $episode_round episodes", "episode", "average win step",
                                               yForceZeroInRange = false))
      afterStartup = { gc ->
        latch.countDown()
      }
    }
    Application.launch(D2DGameUI::class.java)
  }
}