@file:Suppress("UNCHECKED_CAST", "NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.on_policy

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.EpsilonGreedyFunctionPolicy
import lab.mars.rl.problem.MountainCar.CarState
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.util.*
import lab.mars.rl.util.math.max
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
import lab.mars.rl.util.ui.D3DChartUI.D3DChart
import org.junit.Test
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
        Qfunc = func,
        π = EpsilonGreedyFunctionPolicy(func, 0.0),
        α = 0.3 / 8,
        episodes = 9000,
        episodeListener = episode@{ episode, _, _, _ ->
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
          Qfunc = func,
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
              Qfunc = func,
              π = EpsilonGreedyFunctionPolicy(func, 0.0),
              α = α / numTilings,
              episodes = episodes,
              episodeListener = { episode, step, _, _ ->
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
}