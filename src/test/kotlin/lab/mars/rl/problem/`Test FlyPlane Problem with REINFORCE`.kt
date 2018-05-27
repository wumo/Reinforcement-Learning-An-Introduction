@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.problem

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import lab.mars.rl.algo.EpisodeListener
import lab.mars.rl.algo.StepListener
import lab.mars.rl.algo.func_approx.play
import lab.mars.rl.algo.policy_gradient.`Actor-Critic with Eligibility Traces (episodic)`
import lab.mars.rl.algo.policy_gradient.`REINFORCE with Baseline (episodic)`
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.SoftmaxpPolicy
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.math.Vector2
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.D2DGameUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test FlyPlane Problem with REINFORCE` {
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
    val unit = FlyPlane.width / resolution
    val qvalue = Array(resolution) { Array(resolution) { Double.NEGATIVE_INFINITY } }
    var accuG = 0.0
    var wins = 0.0
    var win_step = 0.0
    val episode_round = 100
    val step_round = 10
    val max_episode = 100000
    var episode_base = 0
    var animate = false
    val latch = CountDownLatch(1)
    thread {
      latch.await()
      
      val episodeListener: EpisodeListener = { episode, step, st, G ->
        println(policyFeature.data.size)
        accuG += G
        st as FlyPlane.PlaneState
        if (st.isAtTarget) {
          wins++
          win_step += step
        }
        if (episode % episode_round == 0) {
          val averageG = accuG / episode_round
          val win_rate = wins / episode_round
          val average_win_step = if (wins <= 0) 0.0 else win_step / wins
          accuG = 0.0
          wins = 0.0
          win_step = 0.0
          Platform.runLater {
            D2DGameUI.charts[0].data.add(XYChart.Data(episode + episode_base, averageG))
            D2DGameUI.charts[1].data.add(XYChart.Data(episode + episode_base, win_rate))
            if (average_win_step > 0)
              D2DGameUI.charts[2].data.add(XYChart.Data(episode + episode_base, average_win_step))
          }
        }
      }
      val stepListener: StepListener = step@{ episode, step, s, a ->
        s as FlyPlane.PlaneState
        a as DefaultAction<Int, FlyPlane.PlaneState>
        
        val nx = Math.floor(s.loc.x / unit).toInt()
        val ny = Math.floor(s.loc.y / unit).toInt()
        qvalue[nx][ny] = maxOf(qvalue[nx][ny], v(s))
        
        if (episode % step_round != 0) return@step
        if (!animate && step > 1) return@step
        D2DGameUI.render { gc ->
          gc.clearRect(0.0, 0.0, FlyPlane.width, FlyPlane.width)
          var max = Double.NEGATIVE_INFINITY
          var min = Double.POSITIVE_INFINITY
          for (nx in 0 until resolution)
            for (ny in 0 until resolution) {
              val q = qvalue[nx][ny]
              if (q.isInfinite()) continue
              max = maxOf(max, q)
              min = minOf(min, q)
            }
          for (nx in 0 until resolution)
            for (ny in 0 until resolution) {
              var q = qvalue[nx][ny]
              if (q.isInfinite())
                q = min
              gc.fill = Color.BLUE.interpolate(Color.RED, if (max == min) 0.5 else (q - min) / (max - min))
              gc.fillRect(nx * unit, ny * unit, unit, unit)
            }
          D2DGameUI.title = "max=$max,min=$min"
          with(s) {
            gc.stroke = Color.GREEN
            gc.lineWidth = 3.0
            gc.strokeOval(targetLoc.x - targetRadius, targetLoc.y - targetRadius, 2 * targetRadius, 2 * targetRadius)
            gc.stroke = Color.RED
            gc.strokeOval(oLoc.x - oRadius, oLoc.y - oRadius, 2 * oRadius, 2 * oRadius)
            gc.fill = Color.BLACK
            val dir = s.vel.copy().norm() * lab.mars.rl.problem.FlyPlane.planeRadius
            val top = s.loc + dir
            val left = s.loc + (dir / 2.0).rot90L()
            val right = s.loc + (dir / 2.0).rot90R()
            gc.fillPolygon(kotlin.doubleArrayOf(top.x, left.x, right.x),
                           kotlin.doubleArrayOf(top.y, left.y, right.y),
                           3)
          }
        }
      }
      while (true) {
        val prob = FlyPlane.make(obstacleLoc = FlyPlane.oLoc + Vector2(10.0, 0.0).rotate(Rand().nextDouble() * 360) * Rand().nextDouble())
        animate = false
        prob.`REINFORCE with Baseline (episodic)`(
            h = h, α_θ = 1e-12 / numTilings,
            v = v, α_w = 0.6 / numTilings,
            episodes = max_episode,
            episodeListener = episodeListener,
            stepListener = stepListener
        )
        animate = true
        prob.play(
            π = SoftmaxpPolicy(h),
            episodes = 10,
            stepListener = { _, _, s, a ->
              stepListener(0, 0, s, a)
              Thread.sleep(Math.floor(1000 / 60.0).toLong())
            }
        )
        episode_base += max_episode
      }
    }
    D2DGameUI.apply {
      canvas_width = FlyPlane.width
      canvas_height = FlyPlane.width
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