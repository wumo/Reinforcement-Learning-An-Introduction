@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.algo.eligibility_trace.control

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.func_approx.on_policy.`Episodic semi-gradient Sarsa control`
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.EpsilonGreedyFunctionPolicy
import lab.mars.rl.problem.CarState
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.util.*
import lab.mars.rl.util.range.rangeTo
import lab.mars.rl.util.range.step
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class `Test Optimal Sarsa λ` {
  val numTilings = 8
  val positionScale = numTilings / (MountainCar.POSITION_MAX - MountainCar.POSITION_MIN)
  val velocityScale = numTilings / (MountainCar.VELOCITY_MAX - MountainCar.VELOCITY_MIN)
  
  fun func(): LinearFunc<tuple2<DoubleArray, IntArray>> {
    val feature = SuttonTileCoding(511, numTilings) { (s, a) ->
      s as CarState
      a as DefaultAction<Int, CarState>
      tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity),
             intArrayOf(a.value))
    }
    return LinearFunc(feature)
  }
  
  @Test
  fun `Mountain Car UI`() {
    val prob = MountainCar.make()
    
    val episodes = intArrayOf(1, 12, 104, 1000, 9000)
    val latch = CountDownLatch(1)
    thread {
      latch.await()
      val Qfunc = func()
      prob.`True Online Sarsa(λ)`(
          Qfunc = Qfunc,
          π = EpsilonGreedyFunctionPolicy(Qfunc, 0.0),
          λ = 0.96,
          α = 0.3 / numTilings,
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
  fun `Early performance on the Mountain Car`() {
    logLevel(Level.ERROR)
    val prob = MountainCar.make()
    
    val episodes = 50
    val runs = 100
    val αs = 0.25..1.75 step 0.25
    val λs = listOf(0.0, 0.68, 0.84, 0.92, 0.96, 0.98, 0.99)
    
    val chart = LineChart("Early performance on the Mountain Car task of Sarsa(λ)",
                          "α x number of tilings (8)", "steps per episode",
                          yAxisConfig = {
                            isAutoRanging = false
                            upperBound = 300.0
                            lowerBound = 165.0
                          })
    runBlocking {
      for (λ in λs) {
        val line = Line("λ=$λ ")
        asyncs(αs) { α ->
          var totalStep = 0.0
          asyncs(runs) { run ->
            val Qfunc = func()
            var step = 0
            prob.`Sarsa(λ) linear trace`(
                traceOp = `replacing trace`,
                Q = Qfunc,
                π = EpsilonGreedyFunctionPolicy(Qfunc, 0.0),
                λ = λ,
                α = α / numTilings,
                episodes = episodes,
                maxStep = 5000,
                episodeListener = { _, _step ->
                  step += _step
                })
            println("finish \tλ=$λ \tα=${α.format(2)} \trun=$run \tstep=$step")
            step
          }.await {
            totalStep += it
          }
          totalStep /= (runs * episodes)
          println("finish \tλ=$λ \tα=${α.format(2)} \ttotal=${totalStep.format(2)}")
          tuple2(α, totalStep)
        }.await { (alpha, step) ->
          line[alpha] = step
        }
        chart += line
        println("finish \tλ=$λ")
      }
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
  
  @Test
  fun `Comparison`() {
    logLevel(Level.ERROR)
    val prob = MountainCar.make()
    
    val episodes = 20
    val runs = 30
    val αs = 0.2..2.0 step 0.2
    val λ = 0.9
    val traces = listOf(`dutch trace`,
                        `replacing trace`,
                        `replacing trace with clearing`,
                        `accumulating trace`)
    val trace_description = listOf("dutch trace",
                                   "replacing trace",
                                   "replacing trace with clearing",
                                   "accumulating trace",
                                   "true online Sarsa(λ)")
    val chart = LineChart("Summary comparison of Sarsa(λ)",
                          "α x number of tilings (8)",
                          "reward per episode",
                          yAxisConfig = {
                            isAutoRanging = false
                            tickUnit = 50.0
                            upperBound = -150.0
                            lowerBound = -550.0
                          })
    runBlocking {
      for ((idx, trace_desc) in trace_description.withIndex()) {
        val line = Line(trace_desc)
        asyncs(αs) { α ->
          if (trace_desc == "accumulating trace" && α > 0.6)
            return@asyncs tuple2(α, -560.0)
          var totalReward = 0.0
          asyncs(runs) { run ->
            var reward = 0
            val Qfunc = func()
            if (idx <= traces.lastIndex)
              prob.`Sarsa(λ) linear trace`(
                  traceOp = traces[idx],
                  Q = Qfunc,
                  π = EpsilonGreedyFunctionPolicy(Qfunc, 0.0),
                  λ = λ,
                  α = α / numTilings,
                  episodes = episodes,
                  maxStep = 5000,
                  episodeListener = { _, _step ->
                    reward += -_step//reward is negative step
                  })
            else
              prob.`True Online Sarsa(λ)`(
                  Qfunc = Qfunc,
                  π = EpsilonGreedyFunctionPolicy(Qfunc, 0.0),
                  λ = λ,
                  α = α / numTilings,
                  episodes = episodes,
                  maxStep = 5000,
                  episodeListener = { _, _step ->
                    reward += -_step//reward is negative step
                  })
            println("finish \t$trace_desc" +
                    "\tα=${α.format(2)} \trun=$run \treward=$reward")
            reward
          }.await {
            totalReward += it
          }
          totalReward /= (runs * episodes)
          println("finish \t$trace_desc" +
                  "\tα=${α.format(2)} \ttotal=${totalReward.format(2)}")
          tuple2(α, totalReward)
        }.await { (alpha, reward) ->
          line[alpha] = reward
        }
        chart += line
        println("finish \t$trace_desc")
      }
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}