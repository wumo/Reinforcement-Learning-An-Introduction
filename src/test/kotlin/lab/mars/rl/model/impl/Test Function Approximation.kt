@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.model.impl

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.`Gradient Monte Carlo algorithm`
import lab.mars.rl.algo.func_approx.`Semi-gradient TD(0)`
import lab.mars.rl.algo.func_approx.`n-step semi-gradient TD`
import lab.mars.rl.algo.td.TemporalDifference
import lab.mars.rl.algo.td.prediction
import lab.mars.rl.model.State
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.problem.SquareWave.domain
import lab.mars.rl.problem.SquareWave.maxResolution
import lab.mars.rl.problem.SquareWave.sample
import lab.mars.rl.problem.`1000-state RandomWalk`.make
import lab.mars.rl.problem.`1000-state RandomWalk`.num_states
import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.ui.ChartApp
import lab.mars.rl.util.ui.ChartView
import lab.mars.rl.util.ui.chart
import lab.mars.rl.util.ui.line
import org.apache.commons.math3.util.FastMath.pow
import org.apache.commons.math3.util.FastMath.sqrt
import org.junit.Test

class `Test Function Approximation` {
    class `1000-state Random walk problem` {
        @Test
        fun `Gradient Monte Carlo`() {
            val chart = chart("V")
            val (prob, PI) = make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 100000
            val V = algo.prediction()
            prob.apply {
                val line = line("TD")
                for (s in states) {
                    println("${V[s].format(2)} ")
                    line[s[0]] = V[s]
                }
                chart += line
            }

            val algo2 = FunctionApprox(prob, PI)
            algo2.episodes = 100000
            algo2.α = 2e-5
            val func = StateAggregationValueFunction(num_states + 2, 10)
            algo2.`Gradient Monte Carlo algorithm`(func)
            prob.apply {
                val line = line("gradient MC")
                for (s in states) {
                    println("${func.invoke(s).format(2)} ")
                    line[s[0]] = func.invoke(s)
                }
                chart += line
            }
            ChartView.charts += chart
            Application.launch(ChartApp::class.java)
        }

        @Test
        fun `Semi-gradient TD(0)`() {
            val chart = chart("V")
            val (prob, PI) = make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 100000
            val V = algo.prediction()
            prob.apply {
                val line = line("TD")
                for (s in states) {
                    println("${V[s].format(2)} ")
                    line[s[0]] = V[s]
                }
                chart += line
            }

            val algo2 = FunctionApprox(prob, PI)
            algo2.episodes = 100000
            algo2.α = 2e-4
            val func = StateAggregationValueFunction(num_states + 2, 10)
            algo2.`Semi-gradient TD(0)`(func)
            prob.apply {
                val line = line("Semi-gradient TD(0)")
                for (s in states) {
                    println("${func.invoke(s).format(2)} ")
                    line[s[0]] = func.invoke(s)
                }
                chart += line
            }
            ChartView.charts += chart
            Application.launch(ChartApp::class.java)
        }

        @Test
        fun `n-step semi-gradient TD`() {
            val chart = chart("V")
            val (prob, PI) = make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 100000
            val V = algo.prediction()
            prob.apply {
                val line = line("TD")
                for (s in states) {
                    println("${V[s].format(2)} ")
                    line[s[0]] = V[s]
                }
                chart += line
            }

            val algo2 = FunctionApprox(prob, PI)
            algo2.episodes = 100000
            algo2.α = 2e-4
            val func = StateAggregationValueFunction(num_states + 2, 10)
            algo2.`n-step semi-gradient TD`(10, func)
            prob.apply {
                val line = line("n-step semi-gradient TD")
                for (s in states) {
                    println("${func.invoke(s).format(2)} ")
                    line[s[0]] = func.invoke(s)
                }
                chart += line
            }
            ChartView.charts += chart
            Application.launch(ChartApp::class.java)
        }

        @Test
        fun `Gradient Monte Carlo with Fourier basis vs polynomials`() {
            logLevel(Level.ERROR)

            val (prob, PI) = make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 100000
            val V = algo.prediction()

            fun RMS(f: ValueFunction): Double {
                var result = 0.0
                for (s in prob.states)
                    result += pow(V[s] - f.invoke(s), 2)
                result /= prob.states.size
                return sqrt(result)
            }

            val chart = chart("RMS")
            val episodes = 5000
            val runs = 5
            val description = listOf("polynomial", "fourier")
            val alphas = listOf(1e-4, 5e-5)
            val func_maker = listOf({ order: Int -> SimplePolynomial(order + 1, 1.0 / num_states) },
                                    { order: Int -> SimpleFourier(order + 1, 1.0 / num_states) })
            val orders = intArrayOf(5, 10, 20)
            val outerChan = Channel<Boolean>(orders.size * alphas.size)
            runBlocking {
                for (func_id in 0..1)
                    for (order in orders) {
                        launch {
                            val runChan = Channel<DoubleArray>(runs)
                            for (run in 1..runs)
                                launch {
                                    val algo = FunctionApprox(prob, PI)
                                    algo.episodes = episodes
                                    val _errors = DoubleArray(episodes) { 0.0 }
                                    val func = LinearFunc(func_maker[func_id](order), alphas[func_id])
                                    algo.episodeListener = { episode ->
                                        _errors[episode - 1] += RMS(func)
                                    }
                                    algo.`Gradient Monte Carlo algorithm`(func)
                                    runChan.send(_errors)
                                }
                            val errors = DoubleArray(episodes) { 0.0 }
                            repeat(runs) {
                                val _errors = runChan.receive()
                                _errors.forEachIndexed { episode, e ->
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
                        }
                    }
                repeat(orders.size * 2) {
                    outerChan.receive()
                }
            }
            ChartView.charts += chart
            Application.launch(ChartApp::class.java)
        }

    }

    @Test
    fun `Coarse Coding`() {
        val alpha = 0.2
        val numOfSamples = listOf(10, 40, 160, 2560, 10240)
        val featureWidths = listOf(0.2, .4, 1.0)
        for (numOfSample in numOfSamples) {
            val chart = chart("$numOfSample samples")
            for (featureWidth in featureWidths) {
                val line = line("feature width: ${featureWidth.format(1)}")
                val func = LinearFunc(SimpleCoarseCoding(featureWidth,
                                                         domain,
                                                         2.0 / maxResolution,
                                                         50), alpha)
                repeat(numOfSample) {
                    val (s, y) = sample()
                    func.update(s, y - func.invoke(s))
                }
                for (i in 0 until maxResolution) {
                    val s = State(DefaultIntBuf.of(i))
                    val y = func.invoke(s)
                    line[i * 2.0 / maxResolution] = y
                }
                chart += line
            }
            ChartView.charts += chart
        }
        Application.launch(ChartApp::class.java)
    }
}