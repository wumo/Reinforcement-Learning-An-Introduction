@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.algo.eligibility_trace.control

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.func_approx.on_policy.`Episodic semi-gradient n-step Sarsa control`
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

class `Test Optimal Sarsa λ` {
  @Test
  fun `Early performance on the Mountain Car`() {
    logLevel(Level.ERROR)
    val prob = MountainCar.make()
    
    val numTilings = 8
    val positionScale = numTilings / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
    val velocityScale = numTilings / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
    val episodes = 50
    val runs = 30
    val αs = listOf(10) { 0.1 + it * 0.14 }
    val λs = listOf(0.0, 0.68, 0.84, 0.92, 0.96, 0.98, 0.99)
    
    val chart = chart("Effect of the α and n on early performance",
                      "α x number of tilings (8)", "steps per episode")
    val truncateStep = 300.0
    runBlocking {
      for (λ in λs) {
        val line = line("λ=$λ ")
        asyncs(αs) { α ->
          var totalStep = 0.0
          asyncs(runs) {run->
            val feature = SuttonTileCoding(511, numTilings) { (s, a) ->
              s as CarState
              a as DefaultAction<Int, CarState>
              tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
            }
            val func = LinearFunc(feature)
            var step = 0
            prob.`Sarsa(λ) replacing trace`(
                Q = func,
                π = `ε-greedy function policy`(func, 0.0),
                λ = λ,
                α = α / numTilings,
                episodes = episodes,
                episodeListener = { _, _step ->
                  step += _step
                })
            println("finish λ=$λ α=$α run=$run step=$step")
            step
          }.await {
            totalStep += it
          }
          totalStep /= (runs * episodes)
          println("finish λ=$λ α=$α total=$totalStep")
          tuple2(α, totalStep)
        }.await { (alpha, step) ->
          if (step < truncateStep)
            line[alpha] = step
        }
        chart += line
        println("finish λ=$λ")
      }
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}