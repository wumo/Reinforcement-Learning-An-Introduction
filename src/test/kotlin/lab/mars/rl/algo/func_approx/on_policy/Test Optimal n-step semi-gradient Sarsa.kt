@file:Suppress("UNCHECKED_CAST", "NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.on_policy

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.`ε-greedy function policy`
import lab.mars.rl.problem.CarState
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.util.*
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
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
    val alphas = listOf(0.5, 0.3)
    val nSteps = listOf(1, 8)
    
    val chart = chart("One-step vs multi-step performance", "episode", "steps per episode")
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
              π = `ε-greedy function policy`(func, 0.0),
              n = n,
              α = alphas[i] / numTilings,
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
        val line = line("MountainCar episodic sarsa ($n) ")
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
    val αs = listOf(10) { 0.1 + it * 0.14 }
    val nSteps = listOf(1, 2, 4, 8, 16)
    
    val chart = chart("Effect of the α and n on early performance",
                      "α x number of tilings (8)", "steps per episode")
    val truncateStep = 300.0
    runBlocking {
      for (n in nSteps) {
        val line = line("n=$n ")
        asyncs(αs) { α ->
          if ((n == 8 && α > 1) || (n == 16 && α > 0.75))
            return@asyncs tuple2(α, truncateStep)
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
                π = `ε-greedy function policy`(func, 0.0),
                n = n,
                α = α / numTilings,
                episodes = episodes,
                episodeListener = { _, _step ->
                  step += _step
                })
            println("finish n=$n α=$α run=$run step=$step")
            step
          }.await {
            totalStep += it
          }
          totalStep /= (runs * episodes)
          println("finish n=$n α=$α total=$totalStep")
          tuple2(α, totalStep)
        }.await { (alpha, step) ->
          if (step < truncateStep)
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