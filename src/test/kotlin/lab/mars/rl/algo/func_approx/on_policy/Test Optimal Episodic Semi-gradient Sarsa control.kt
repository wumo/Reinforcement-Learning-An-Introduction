@file:Suppress("UNCHECKED_CAST", "NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.on_policy

import ch.qos.logback.classic.Level
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.eligibility_trace.control.`True Online Sarsa(λ)`
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.EpsilonGreedyFunctionPolicy
import lab.mars.rl.problem.FlyPlane
import lab.mars.rl.problem.FlyPlane.PlaneState
import lab.mars.rl.problem.FlyPlane.width
import lab.mars.rl.problem.MountainCar.CarState
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.util.*
import lab.mars.rl.util.math.max
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
import lab.mars.rl.util.ui.D2DGameUI.Companion.afterStartup
import lab.mars.rl.util.ui.D2DGameUI.Companion.title
import lab.mars.rl.util.ui.D3DChartUI.D3DChart
import org.junit.Test
import java.lang.Math.floor
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test Optimal Episodic Semi-gradient Sarsa control` {
  @Test
  fun `Mountain Car`() {
    val prob = MountainCar.make()
    val positionScale = 8 / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
    val velocityScale = 8 / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
    val feature = SuttonTileCoding(511, 8) { (s, a) ->
      s as CarState
      a as DefaultAction<Int, CarState>
      tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
    }
    val func = LinearFunc(feature)
    
    val episodes = intArrayOf(1, 12, 104, 1000, 9000)
    prob.`Episodic semi-gradient Sarsa control`(
        Q = func,
        π = EpsilonGreedyFunctionPolicy(func, 0.0),
        α = 0.3 / 8,
        episodes = 9000,
        episodeListener = episode@{ episode, _ ->
          if (episode !in episodes) return@episode
          val _feature = SuttonTileCoding(511, 8) { (s, a) ->
            s as CarState
            a as DefaultAction<Int, CarState>
            tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
          }
          _feature.data.putAll(feature.data)
          val _func = LinearFunc(_feature)
          _func.w `=` func.w
          val chart = D3DChart("Episode $episode", "Position", "Velocity", "Value",
                               40, 40,
                               MountainCar.POSITION_MIN..MountainCar.POSITION_MAX,
                               MountainCar.VELOCITY_MIN..MountainCar.VELOCITY_MAX,
                               0.0..120.0, 10.0, 10.0, 5.0) { x, y ->
            if (x !in MountainCar.POSITION_MIN..MountainCar.POSITION_MAX || y !in MountainCar.VELOCITY_MIN..MountainCar.VELOCITY_MAX)
              return@D3DChart Double.NaN
            val f = doubleArrayOf(positionScale * x, velocityScale * y)
            val cost = -max(-1..1) { _func._invoke(tuple2(f, intArrayOf(it))) }
            cost
          }
          D3DChartUI.charts += chart
        })
    D3DChartUI.title = "The Mountain Car task"
    Application.launch(D3DChartUI::class.java)
  }
  
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
      prob.`Episodic semi-gradient Sarsa control`(
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
  
  @Test
  fun `Learning curves`() {
    logLevel(Level.ERROR)
    val prob = MountainCar.make()
    
    val numTilings = 8
    val positionScale = numTilings / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
    val velocityScale = numTilings / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
    val episodes = 500
    val runs = 10
    val αs = listOf(0.1, 0.2, 0.5)
    
    val chart = LineChart("Learning curves", "episode", "steps per episode")
    runBlocking {
      asyncs(αs) { α ->
        val steps = IntArray(episodes)
        asyncs(runs) {
          val feature = SuttonTileCoding(511, numTilings) { (s, a) ->
            s as CarState
            a as DefaultAction<Int, CarState>
            tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
          }
          val func = LinearFunc(feature)
          val steps = IntArray(episodes)
          prob.`Episodic semi-gradient Sarsa control`(
              Q = func,
              π = EpsilonGreedyFunctionPolicy(func, 0.0),
              α = α / numTilings,
              episodes = episodes,
              episodeListener = { episode, step ->
                steps[episode - 1] += step
              })
          steps
        }.await {
          it.forEachIndexed { episode, s ->
            steps[episode] += s
          }
          println("finish alpha ($α ) run: 1")
        }
        val line = Line("MountainCar episodic sarsa ($α) ")
        for (episode in 1..episodes)
          line[episode] = steps[episode - 1] / runs.toDouble()
        chart += line
        println("finish MountainCar episodic sarsa ($α)")
      }.await()
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
  
  @Test
  fun `Fly Plane UI`() {
    val prob = FlyPlane.make()
    val numTilings = 8
    val feature = SuttonTileCoding(500, numTilings, doubleArrayOf(0.5, 0.5, 1.0, 1.0)) { (s, a) ->
      s as PlaneState
      a as DefaultAction<Int, PlaneState>
      tuple2(doubleArrayOf(s.loc.x, s.loc.y, s.vel.x, s.vel.y), intArrayOf(a.value))
    }
    val func = LinearFunc(feature)
    
    val resolution = 100
    val unit = width / resolution
    val qvalue = Array(resolution) { Array(resolution) { -1000.0 } }
    var accuG = 0.0
    var wins = 0.0
    var win_step = 0.0
    val round = 1000
    
    val latch = CountDownLatch(1)
    thread {
      latch.await()
      
      prob.`True Online Sarsa(λ)`(
          Qfunc = func,
          π = EpsilonGreedyFunctionPolicy(func, 0.1),
          λ = 0.96,
          α = 1.2 / numTilings,
          episodes = 1000000,
          episodeListener = { episode, step, st, G ->
            accuG += G
            st as PlaneState
            if (st.isAtTarget) {
              wins++
              win_step += step
            }
            if (episode % round == 0) {
              val averageG = accuG / round
              val win_rate = wins / round
              val average_win_step = if (wins <= 0) 0.0 else win_step / wins
              accuG = 0.0
              wins = 0.0
              win_step = 0.0
              Platform.runLater {
                D2DGameUI.charts[0].data.add(XYChart.Data(episode, averageG))
                D2DGameUI.charts[1].data.add(XYChart.Data(episode, win_rate))
                if (average_win_step > 0)
                  D2DGameUI.charts[2].data.add(XYChart.Data(episode, average_win_step))
              }
            }
          },
          stepListener = step@{ episode, _, s, a ->
            s as PlaneState
            a as DefaultAction<Int, PlaneState>
            
            val nx = floor(s.loc.x / unit).toInt()
            val ny = floor(s.loc.y / unit).toInt()
            qvalue[nx][ny] = maxOf(qvalue[nx][ny], func(s, a))
            
            if (episode % round != 0) return@step
            D2DGameUI.render { gc ->
              gc.clearRect(0.0, 0.0, FlyPlane.width, FlyPlane.height)
              var max = 1000.0
              var min = Double.POSITIVE_INFINITY
              for (nx in 0 until resolution)
                for (ny in 0 until resolution) {
                  val q = qvalue[nx][ny]
                  max = maxOf(max, q)
                  min = minOf(min, q)
                  gc.fill = Color.BLUE.interpolate(Color.RED, if (max == min) 0.5 else (q - min) / (max - min))
                  gc.fillRect(nx * unit, ny * unit, unit, unit)
                }
              title = "max=$max,min=$min"
              with(s) {
                gc.stroke = Color.GREEN
                gc.lineWidth = 3.0
                gc.strokeOval(targetLoc.x - targetRadius, targetLoc.y - targetRadius, 2 * targetRadius, 2 * targetRadius)
                gc.stroke = Color.RED
                gc.strokeOval(oLoc.x - oRadius, oLoc.y - oRadius, 2 * oRadius, 2 * oRadius)
                gc.fill = Color.BLACK
                val dir = s.vel.copy().norm() * FlyPlane.planeRadius
                val top = s.loc + dir
                val left = s.loc + (dir / 2.0).rot90L()
                val right = s.loc + (dir / 2.0).rot90R()
                gc.fillPolygon(doubleArrayOf(top.x, left.x, right.x),
                               doubleArrayOf(top.y, left.y, right.y),
                               3)
              }
            }
          })
    }
    D2DGameUI.apply {
      canvas_width = FlyPlane.width
      canvas_height = FlyPlane.height
      width = 1200.0
      height = 800.0
      charts.addAll(D2DGameUI.ChartDescription("average return per $round episodes", "episode", "average return"),
                    D2DGameUI.ChartDescription("win rate per $round episodes", "episode", "win rate"),
                    D2DGameUI.ChartDescription("average win step per $round episodes", "episode", "average win step",
                                               yForceZeroInRange = false))
      afterStartup = { gc ->
        latch.countDown()
      }
    }
    Application.launch(D2DGameUI::class.java)
  }
}