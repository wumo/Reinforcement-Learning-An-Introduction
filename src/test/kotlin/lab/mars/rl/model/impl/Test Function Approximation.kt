@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.model.impl

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.prediction.*
import lab.mars.rl.algo.td.TemporalDifference
import lab.mars.rl.algo.td.prediction
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.model.impl.func.*
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.model.isTerminal
import lab.mars.rl.problem.SquareWave.domain
import lab.mars.rl.problem.SquareWave.maxResolution
import lab.mars.rl.problem.SquareWave.sample
import lab.mars.rl.problem.WaveState
import lab.mars.rl.problem.`1000-state RandomWalk`.make
import lab.mars.rl.problem.`1000-state RandomWalk`.num_states
import lab.mars.rl.util.matrix.times
import lab.mars.rl.util.ui.*
import org.apache.commons.math3.util.FastMath.*
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
            val func = StateAggregationValueFunction(num_states + 2, 10, { (it as IndexedState)[0] })
            algo2.`Gradient Monte Carlo algorithm`(func)
            prob.apply {
                val line = line("gradient MC")
                for (s in states) {
                    println("${func(s).format(2)} ")
                    line[s[0]] = func(s)
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
            val func = StateAggregationValueFunction(num_states + 2, 10, { (it as IndexedState)[0] })
            algo2.`Semi-gradient TD(0)`(func)
            prob.apply {
                val line = line("Semi-gradient TD(0)")
                for (s in states) {
                    println("${func(s).format(2)} ")
                    line[s[0]] = func(s)
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
            val func = StateAggregationValueFunction(num_states + 2, 10, { (it as IndexedState)[0] })
            algo2.`n-step semi-gradient TD`(10, func)
            prob.apply {
                val line = line("n-step semi-gradient TD")
                for (s in states) {
                    println("${func(s).format(2)} ")
                    line[s[0]] = func(s)
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
                for (s in prob.states) {
                    if (s.isTerminal()) continue
                    result += pow(V[s] - f(s), 2)
                }
                result /= prob.states.size
                return sqrt(result)
            }

            val chart = chart("RMS")
            val episodes = 5000
            val runs = 5
            val description = listOf("polynomial", "fourier")
            val alphas = listOf(1e-4, 5e-5)
            val func_maker = listOf({ order: Int -> SimplePolynomial(order + 1, { (it as IndexedState)[0] * 1.0 / num_states }) },
                                    { order: Int -> SimpleFourier(order + 1, { (it as IndexedState)[0] * 1.0 / num_states }) })
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
                                    algo.α = alphas[func_id]
                                    val _errors = DoubleArray(episodes) { 0.0 }
                                    val func = LinearFunc(func_maker[func_id](order))
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
                val feature = SimpleCoarseCoding(featureWidth,
                                                 domain,
                                                 { (it as WaveState).x },
                                                 50)
                val func = LinearFunc(feature)
                repeat(numOfSample) {
                    val (s, y) = sample()
                    func.w += feature.alpha(alpha, s) * (y - func(s)) * func.`▽`(s)
                }
                for (i in 0 until maxResolution) {
                    val s = WaveState(i * 2.0 / maxResolution)
                    val y = func(s)
                    line[i * 2.0 / maxResolution] = y
                }
                chart += line
            }
            ChartView.charts += chart
        }
        Application.launch(ChartApp::class.java)
    }

    @Test
    fun `Tile Coding`() {
        val chart = chart("samples")
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

        val alpha = 1e-4
        val numOfTilings = 50
        val feature = SimpleTileCoding(numOfTilings,
                                       5,
                                       ceil(num_states / 5.0).toInt(),
                                       4.0,
                                       { s -> ((s as IndexedState)[0] - 1).toDouble() })
        val func = LinearFunc(feature)
        val algo2 = FunctionApprox(prob, PI)
        algo2.episodes = 100000
        algo2.α = alpha / numOfTilings
        algo2.`Gradient Monte Carlo algorithm`(func)
        prob.apply {
            val line = line("Tile Coding")
            for (s in states) {
                println("${s[0]}=${func(s).format(2)} ")
                line[s[0]] = func(s)
            }
            chart += line
        }
        ChartView.charts += chart
        Application.launch(ChartApp::class.java)
    }

    @Test
    fun `Tile Coding RMS`() {
        logLevel(Level.ERROR)

        val (prob, PI) = make()
        val algo = TemporalDifference(prob, PI)
        algo.episodes = 100000
        val V = algo.prediction()

        fun RMS(f: ValueFunction): Double {
            var result = 0.0
            for (s in prob.states)
                result += pow(V[s] - f(s), 2)
            result /= prob.states.size
            return sqrt(result)
        }

        val chart = chart("RMS")
        val episodes = 10000
        val runs = 5
        val alpha = 1e-4
        val numOfTilings = intArrayOf(1, 50)
        val outerChan = Channel<Boolean>(numOfTilings.size)
        runBlocking {
            for (numOfTiling in numOfTilings)
                launch {
                    val runChan = Channel<DoubleArray>(runs)
                    for (run in 1..runs)
                        launch {
                            val algo = FunctionApprox(prob, PI)
                            algo.episodes = episodes
                            val _errors = DoubleArray(episodes) { 0.0 }
                            val func = LinearFunc(SimpleTileCoding(numOfTiling,
                                                                   5,
                                                                   ceil(prob.states.size / 5.0).toInt(),
                                                                   4.0,
                                                                   { s -> ((s as IndexedState)[0] - 1).toDouble() }))
                            algo.α = alpha / numOfTiling
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
                        println("finish Tile coding ($numOfTiling tilings) run: 1")
                    }
                    val line = line("Tile coding ($numOfTiling tilings) ")
                    for (episode in 1..episodes) {
                        line[episode] = errors[episode - 1] / runs
                    }
                    chart += line
                    println("finish Tile coding ($numOfTiling tilings)")
                    outerChan.send(true)
                }
            repeat(numOfTilings.size) {
                outerChan.receive()
            }
        }
        ChartView.charts += chart
        Application.launch(ChartApp::class.java)
    }
}