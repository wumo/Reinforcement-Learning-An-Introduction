@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.algo.func_approx.on_policy

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.`ε-greedy function policy`
import lab.mars.rl.problem.CarState
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.util.logLevel
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
import org.junit.Test

class `Test Optimal n-step semi-gradient Sarsa` {
  @Test
  fun `One-step vs multi-step performance`() {
    logLevel(Level.ERROR)
    val mdp = MountainCar.make()

    val numTilings = 8
    val positionScale = numTilings / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
    val velocityScale = numTilings / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
    val episodes = 500
    val runs = 10
    val alphas = listOf(0.5, 0.3)
    val nSteps = listOf(1, 8)

    val chart = chart("One-step vs multi-step performance", "episode", "steps per episode")
    val outerChan = Channel<Boolean>(nSteps.size)
    runBlocking {
      for ((i, n) in nSteps.withIndex())
        async {
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
              algo.α = alphas[i] / numTilings
              val steps = IntArray(episodes)
              algo.episodeListener = { episode, step ->
                steps[episode - 1] += step
              }
              algo.`Episodic semi-gradient n-step Sarsa control`(func, n)
              runChan.send(steps)
            }
          }
          val steps = IntArray(episodes)
          repeat(runs) {
            val _steps = runChan.receive()
            _steps.forEachIndexed { episode, s ->
              steps[episode] += s
            }
            println("finish alpha ($n ) run: 1")
          }
          val line = line("MountainCar episodic sarsa ($n) ")
          for (episode in 1..episodes) {
            line[episode] = steps[episode - 1] / runs.toDouble()
          }
          chart += line
          println("finish MountainCar episodic sarsa ($n)")
          outerChan.send(true)
        }
      repeat(alphas.size) {
        outerChan.receive()
      }
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }

  @Test
  fun `Effect of the α and n on early performance`() {
    logLevel(Level.ERROR)
    val mdp = MountainCar.make()

    val numTilings = 8
    val positionScale = numTilings / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
    val velocityScale = numTilings / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
    val episodes = 50
    val runs = 5
    val alphas = DoubleArray(10) { 0.1 + it * 0.14 }
    val nSteps = listOf(1, 2, 4, 8, 16)

    val chart = chart("Effect of the α and n on early performance",
                      "α x number of tilings (8)", "steps per episode")
    val truncateStep = 300
    for (n in nSteps) {
      val line = line("n=$n ")
      for (alpha in alphas) {
        if ((n == 8 && alpha > 1) || (n == 16 && alpha > 0.75)) {
          continue
        }
        var step = 0
        for (run in 1..runs) {
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
          algo.episodeListener = { _, _step ->
            step += _step
          }
          algo.`Episodic semi-gradient n-step Sarsa control`(func, n)
          println("alpha=$alpha n=$n run:$run")
        }
        val s = step / (runs * episodes).toDouble()
        if (s < truncateStep)
          line[alpha] = s
      }
      chart += line
      println("finish n=$n")
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}