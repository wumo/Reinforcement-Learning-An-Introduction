@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.algo.func_approx.on_policy

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.`ε-greedy function policy`
import lab.mars.rl.problem.CarState
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.util.logLevel
import lab.mars.rl.util.math.max
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
import lab.mars.rl.util.ui.D3DChartUI.D3DChart
import org.junit.Test

class `Test Optimal Episodic Semi-gradient Sarsa control` {
  @Test
  fun `Mountain Car`() {
    val mdp = MountainCar.make()
    val positionScale = 8 / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
    val velocityScale = 8 / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
    val feature = SuttonTileCoding(511, 8) { (s, a) ->
      s as CarState
      a as DefaultAction<Int, CarState>
      tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
    }
    val func = LinearFunc(feature)
    print(feature.numTilings)

    val π = `ε-greedy function policy`(func, 0.0)
    val algo = FunctionApprox(mdp, π)
    algo.episodes = 9000
    val alpha = 0.3
    algo.α = alpha / 8
    val episodes = intArrayOf(1, 12, 104, 1000, 9000)
    D3DChartUI.title = "The Mountain Car task"
    algo.episodeListener = { episode, _ ->
      if (episode in episodes) {
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
      }
    }
    algo.`Episodic semi-gradient Sarsa control`(func)

    Application.launch(D3DChartUI::class.java)
  }

  @Test
  fun `Learning curves`() {
    logLevel(Level.ERROR)
    val mdp = MountainCar.make()

    val numTilings = 8
    val positionScale = numTilings / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
    val velocityScale = numTilings / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
    val episodes = 500
    val runs = 10
    val alphas = listOf(0.1, 0.2, 0.5)

    val chart = chart("Learning curves", "episode", "steps per episode")
    val outerChan = Channel<Boolean>(alphas.size)
    runBlocking {
      for (alpha in alphas)
        launch {
          val runChan = Channel<IntArray>(runs)
          repeat(runs) {
            async {
              val feature = SuttonTileCoding(511, numTilings) { (s, a) ->
                s as CarState
                a as DefaultAction<Int, CarState>
                tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
              }
              val func = LinearFunc(feature)
              val π = `ε-greedy function policy`(func, 0.0)
              val algo = FunctionApprox(mdp, π)
              algo.episodes = episodes
              algo.α = alpha / numTilings
              val steps = IntArray(episodes)
              algo.episodeListener = { episode, step ->
                steps[episode - 1] += step
              }
              algo.`Episodic semi-gradient Sarsa control`(func)
              runChan.send(steps)
            }
          }
          val steps = IntArray(episodes)
          repeat(runs) {
            val _steps = runChan.receive()
            _steps.forEachIndexed { episode, s ->
              steps[episode] += s
            }
            println("finish alpha ($alpha ) run: 1")
          }
          val line = line("MountainCar episodic sarsa ($alpha) ")
          for (episode in 1..episodes) {
            line[episode] = steps[episode - 1] / runs.toDouble()
          }
          chart += line
          println("finish MountainCar episodic sarsa ($alpha)")
          outerChan.send(true)
        }
      repeat(alphas.size) {
        outerChan.receive()
      }
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}