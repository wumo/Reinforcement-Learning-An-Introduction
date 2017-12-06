package lab.mars.rl.algo.ntd

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.problem.`19-state RandomWalk`
import lab.mars.rl.util.*
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
import org.apache.commons.math3.util.FastMath.*
import org.junit.Test

class `Test Prediction n-TD` {
  @Test
  fun `Blackjack`() {
    val (prob, π) = Blackjack.make()
    val V = prob.`N-step TD prediction`(
        n = 102400, π = π,
        episodes = 500000,
        α = 0.1)
    printBlackjack(prob, π, V)
  }
  
  @Test
  fun `RandomWalk`() {
    val (prob, π) = `19-state RandomWalk`.make()
    val V = prob.`N-step TD prediction`(
        n = 8, π = π,
        episodes = 1000, α = 0.1)
    prob.apply {
      for (s in states) {
        println("${V[s].format(2)} ")
      }
    }
  }
  
  @Test
  fun `RandomWalk RMS`() {
    logLevel(Level.ERROR)
    
    val (prob, π) = `19-state RandomWalk`.make()
    val realV = listOf(-20..20 step 2) { it / 20.0 }
    realV[0] = 0.0
    realV[20] = 0.0
    
    val ns = listOf(10) { pow(2.0, it).toInt() }
    val αs = listOf(110) { it * 0.01 }
    
    val episodes = 10
    val runs = 100
    val truncateValue = 0.55
    
    val chart = chart("RMS", "α", "Average RMS")
    runBlocking {
      for (n in ns) {
        val line = line("n=$n")
        chart += line
        asyncs(αs) { α ->
          var rms_sum = 0.0
          asyncs(runs) {
            var rms = 0.0
            prob.`N-step TD prediction`(
                n = n, π = π,
                episodes = episodes,
                α = α,
                episodeListener = { _, V ->
                  var error = 0.0
                  for (s in prob.states)
                    error += pow(V[s] - realV[s[0]], 2)
                  error /= prob.states.size
                  rms += sqrt(error)
                })
            rms
          }.await { rms_sum += it }
          tuple2(α, rms_sum / (episodes * runs))
        }.await { (α, rms) ->
          if (rms < truncateValue)
            line[α] = rms
        }
        println("finish n=$n")
      }
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}