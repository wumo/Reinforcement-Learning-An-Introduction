@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.prediction

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.td.`Tabular TD(0)`
import lab.mars.rl.model.ApproximateFunction
import lab.mars.rl.model.impl.func.*
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.model.isTerminal
import lab.mars.rl.problem.`1000-state RandomWalk`
import lab.mars.rl.util.*
import lab.mars.rl.util.ui.*
import org.apache.commons.math3.util.FastMath.*
import org.junit.Test

class Test {
  @Test
  fun `Fourier basis vs polynomials`() {
    logLevel(Level.ERROR)
    
    val (prob, π) = `1000-state RandomWalk`.make()
    val V = prob.`Tabular TD(0)`(π = π, episodes = 100000, α = 0.1)
    
    fun RMS(f: ApproximateFunction<Double>): Double {
      var result = 0.0
      for (s in prob.states) {
        if (s.isTerminal) continue
        result += pow(V[s] - f(s), 2)
      }
      result /= prob.states.size
      return sqrt(result)
    }
    
    val chart = chart("RMS", "episode", "RMS")
    val episodes = 5000
    val runs = 5
    val description = listOf("polynomial", "fourier")
    val alphas = listOf(1e-4, 5e-5)
    val func_maker = listOf({ order: Int -> SimplePolynomial(order + 1) { (s) -> (s as IndexedState)[0] * 1.0 / `1000-state RandomWalk`.num_states } },
                            { order: Int -> SimpleFourier(order + 1) { (s) -> (s as IndexedState)[0] * 1.0 / `1000-state RandomWalk`.num_states } })
    val orders = listOf(5, 10, 20)
    val outerChan = Channel<Boolean>(orders.size * alphas.size)
    runBlocking {
      asyncs(0..1, orders) { func_id, order ->
        val errors = DoubleArray(episodes) { 0.0 }
        asyncs(runs) {
          val _errors = DoubleArray(episodes) { 0.0 }
          val func = LinearFunc(func_maker[func_id](order))
          prob.`Gradient Monte Carlo algorithm`(
              v = func, π = π,
              episodes = episodes,
              α = alphas[func_id],
              episodeListener = { episode, _ ->
                _errors[episode - 1] += RMS(func)
              })
          _errors
        }.await {
          it.forEachIndexed { episode, e ->
            errors[episode] += e
          }
        }
        val line = line("${description[func_id]} order=$order")
        for (episode in 1..episodes) {
          line[episode] = errors[episode - 1] / runs
        }
        chart += line
        println("finish ${description[func_id]} order=$order")
        outerChan.send(true)
      }.await()
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}