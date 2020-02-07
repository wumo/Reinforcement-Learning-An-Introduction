@file:Suppress("UNCHECKED_CAST", "NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.on_policy

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.runBlocking
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.EpsilonGreedyFunctionPolicy
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.problem.MountainCar.CarState
import lab.mars.rl.util.asyncs
import lab.mars.rl.util.await
import lab.mars.rl.util.format
import lab.mars.rl.util.logLevel
import lab.mars.rl.util.range.rangeTo
import lab.mars.rl.util.range.step
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.ChartApp
import lab.mars.rl.util.ui.D2DChart
import lab.mars.rl.util.ui.Line
import lab.mars.rl.util.ui.LineChart
import org.junit.Test

class `Test Optimal n-step semi-gradient Sarsa` {
  @Test
  fun `One-step vs multi-step performance`() {
    logLevel(Level.ERROR)
    val prob = MountainCar.make()
    
    val numTilings = 8
    val positionScale = numTilings / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
    val velocityScale = numTilings / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
    val episodes = 500
    val runs = 10
    val αs = listOf(0.5, 0.3)
    val nSteps = listOf(1, 8)
    
    val chart = LineChart("One-step vs multi-step performance", "episode", "steps per episode")
    runBlocking {
      asyncs(nSteps.withIndex()) { (i, n) ->
        val steps = IntArray(episodes)
        asyncs(runs) {
          val feature = SuttonTileCoding(511, numTilings) { (s, a) ->
            s as CarState
            a as DefaultAction<Int, CarState>
            tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
          }
          val func = LinearFunc(feature)
          val steps = IntArray(episodes)
          prob.`Episodic semi-gradient n-step Sarsa control`(
              q = func,
              π = EpsilonGreedyFunctionPolicy(func, 0.0),
              n = n,
              α = αs[i] / numTilings,
              episodes = episodes,
              episodeListener = { episode, step ->
                steps[episode - 1] += step
              })
          steps
        }.await {
          it.forEachIndexed { episode, s ->
            steps[episode] += s
          }
          println("finish alpha ($n ) run: 1")
        }
        val line = Line("MountainCar episodic sarsa ($n) ")
        for (episode in 1..episodes)
          line[episode] = steps[episode - 1] / runs.toDouble()
        chart += line
        println("finish MountainCar episodic sarsa ($n)")
      }.await()
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
  
  @Test
  fun `Effect of the α and n on early performance`() {
    logLevel(Level.ERROR)
    val prob = MountainCar.make()
    
    val numTilings = 8
    val positionScale = numTilings / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
    val velocityScale = numTilings / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
    val episodes = 50
    val runs = 5
    val αs = 0.1..1.5 step 0.14
    val nSteps = listOf(1, 2, 4, 8, 16)
    
    val chart = LineChart("Effect of the α and n on early performance",
                          "α x number of tilings (8)", "steps per episode",
                          yAxisConfig = {
                            isAutoRanging = false
                            upperBound = 300.0
                            lowerBound = 210.0
                          })
    runBlocking {
      for (n in nSteps) {
        val line = Line("n=$n ")
        asyncs(αs) { α ->
          var totalStep = 0.0
          asyncs(runs) { run ->
            val feature = SuttonTileCoding(511, numTilings) { (s, a) ->
              s as CarState
              a as DefaultAction<Int, CarState>
              tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
            }
            val func = LinearFunc(feature)
            var step = 0
            prob.`Episodic semi-gradient n-step Sarsa control`(
                q = func,
                π = EpsilonGreedyFunctionPolicy(func, 0.0),
                n = n,
                α = α / numTilings,
                episodes = episodes,
                maxStep = 5000,
                episodeListener = { _, _step ->
                  step += _step
                })
            println("finish n=$n α=${α.format(2)} run=$run step=$step")
            step
          }.await {
            totalStep += it
          }
          totalStep /= (runs * episodes)
          println("finish n=$n α=${α.format(2)} total=$totalStep")
          tuple2(α, totalStep)
        }.await { (alpha, step) ->
          line[alpha] = step
        }
        chart += line
        println("finish n=$n")
      }
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}