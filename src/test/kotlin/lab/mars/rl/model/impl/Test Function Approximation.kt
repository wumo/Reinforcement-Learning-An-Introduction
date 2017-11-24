@file:Suppress("NAME_SHADOWING", "UNCHECKED_CAST")

package lab.mars.rl.model.impl

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.on_policy_control.`Episodic semi-gradient Sarsa control`
import lab.mars.rl.algo.func_approx.on_policy_control.`Episodic semi-gradient n-step Sarsa control`
import lab.mars.rl.algo.func_approx.prediction.*
import lab.mars.rl.algo.td.TemporalDifference
import lab.mars.rl.algo.td.prediction
import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.*
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.problem.*
import lab.mars.rl.problem.MountainCar.POSITION_MAX
import lab.mars.rl.problem.MountainCar.POSITION_MIN
import lab.mars.rl.problem.MountainCar.VELOCITY_MAX
import lab.mars.rl.problem.MountainCar.VELOCITY_MIN
import lab.mars.rl.problem.SquareWave.domain
import lab.mars.rl.problem.SquareWave.maxResolution
import lab.mars.rl.problem.SquareWave.sample
import lab.mars.rl.problem.`1000-state RandomWalk`.make
import lab.mars.rl.problem.`1000-state RandomWalk`.num_states
import lab.mars.rl.util.matrix.times
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
import lab.mars.rl.util.ui.D3DChartUI.Companion.charts
import lab.mars.rl.util.ui.D3DChartUI.D3DChart
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
            val func = StateAggregation(num_states + 2, 10)
            val trans = { s: State -> (s as IndexedState)[0] }
            algo2.`Gradient Monte Carlo algorithm`(func, trans)
            prob.apply {
                val line = line("gradient MC")
                for (s in states) {
                    println("${func(trans(s)).format(2)} ")
                    line[s[0]] = func(trans(s))
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
            val func = StateAggregation(num_states + 2, 10)
            val trans = { s: State -> (s as IndexedState)[0] }
            algo2.`Semi-gradient TD(0)`(func, trans)
            prob.apply {
                val line = line("Semi-gradient TD(0)")
                for (s in states) {
                    println("${func(trans(s)).format(2)} ")
                    line[s[0]] = func(trans(s))
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
            val func = StateAggregation(num_states + 2, 10)
            val trans = { s: State -> (s as IndexedState)[0] }
            algo2.`n-step semi-gradient TD`(10, func, trans)
            prob.apply {
                val line = line("n-step semi-gradient TD")
                for (s in states) {
                    println("${func(trans(s)).format(2)} ")
                    line[s[0]] = func(trans(s))
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

            fun RMS(f: ApproximateFunction<Double>, trans: (State) -> Double): Double {
                var result = 0.0
                for (s in prob.states) {
                    if (s.isTerminal()) continue
                    result += pow(V[s] - f(trans(s)), 2)
                }
                result /= prob.states.size
                return sqrt(result)
            }

            val chart = chart("RMS")
            val episodes = 5000
            val runs = 5
            val description = listOf("polynomial", "fourier")
            val alphas = listOf(1e-4, 5e-5)
            val func_maker = listOf({ order: Int -> SimplePolynomial(order + 1) },
                                    { order: Int -> SimpleFourier(order + 1) })
            val trans = { s: State -> (s as IndexedState)[0] * 1.0 / num_states }
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
                                    algo.episodeListener = { episode, _ ->
                                        _errors[episode - 1] += RMS(func, trans)
                                    }
                                    algo.`Gradient Monte Carlo algorithm`(func, trans)
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
                                           4.0)
            val trans = { s: State -> ((s as IndexedState)[0] - 1).toDouble() }
            val func = LinearFunc(feature)
            val algo2 = FunctionApprox(prob, PI)
            algo2.episodes = 100000
            algo2.α = alpha / numOfTilings
            algo2.`Gradient Monte Carlo algorithm`(func, trans)
            prob.apply {
                val line = line("Tile Coding")
                for (s in states) {
                    println("${s[0]}=${func(trans(s)).format(2)} ")
                    line[s[0]] = func(trans(s))
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

            fun RMS(f: ApproximateFunction<Double>, trans: (State) -> Double): Double {
                var result = 0.0
                for (s in prob.states) {
                    if (s.isTerminal()) continue
                    result += pow(V[s] - f(trans(s)), 2)
                }
                result /= prob.states.size
                return sqrt(result)
            }

            val chart = chart("RMS")
            val episodes = 10000
            val runs = 5
            val alpha = 1e-4
            val numOfTilings = intArrayOf(1, 50)
            val outerChan = Channel<Boolean>(numOfTilings.size)
            val trans = { s: State -> ((s as IndexedState)[0] - 1).toDouble() }
            runBlocking {
                for (numOfTiling in numOfTilings)
                    launch {
                        val runChan = Channel<DoubleArray>(runs)
                        for (run in 1..runs)
                            launch {
                                val func = LinearFunc(SimpleTileCoding(numOfTiling,
                                                                       5,
                                                                       ceil(prob.states.size / 5.0).toInt(),
                                                                       4.0))
                                val algo = FunctionApprox(prob, PI)
                                algo.α = alpha / numOfTiling
                                algo.episodes = episodes
                                val _errors = DoubleArray(episodes)
                                algo.episodeListener = { episode, _ ->
                                    _errors[episode - 1] += RMS(func, trans)
                                }
                                algo.`Gradient Monte Carlo algorithm`(func, trans)
                                runChan.send(_errors)
                            }
                        val errors = DoubleArray(episodes)
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

        @Test
        fun `Sutton Tile Coding `() {
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
            val numOfTilings = 32

            val feature = SuttonTileCoding(5,
                                           numOfTilings)
            val trans = { s: State -> tuple2(doubleArrayOf((s as IndexedState)[0] * 5.0 / num_states), intArrayOf()) }

            val func = LinearFunc(feature)
            val algo2 = FunctionApprox(prob, PI)
            algo2.episodes = 100000
            algo2.α = alpha / numOfTilings
            algo2.`Gradient Monte Carlo algorithm`(func, trans)
            prob.apply {
                val line = line("Tile Coding")
                for (s in states) {
                    println("${s[0]}=${func(trans(s)).format(2)} ")
                    line[s[0]] = func(trans(s))
                }
                chart += line
            }
            println("data size=${feature.data.size}")
            feature.data.forEach { k, v -> println("$k=$v") }
            ChartView.charts += chart
            Application.launch(ChartApp::class.java)
        }

        @Test
        fun `Sutton Tile Coding RMS`() {
            logLevel(Level.ERROR)

            val (prob, PI) = make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 100000
            val V = algo.prediction()

            fun <E> RMS(f: ApproximateFunction<E>, trans: (State) -> E): Double {
                var result = 0.0
                for (s in prob.states) {
                    if (s.isTerminal()) continue
                    result += pow(V[s] - f(trans(s)), 2)
                }
                result /= prob.states.size
                return sqrt(result)
            }

            val chart = chart("RMS")

            val episodes = 10000
            val runs = 5
            val alpha = 1e-4
            val numOfTilings = intArrayOf(4, 32)
            val outerChan = Channel<Boolean>(numOfTilings.size)
            val trans = { s: State -> ((s as IndexedState)[0] - 1).toDouble() }
            runBlocking {
                val numOfTiling = 1
                val runChan = Channel<DoubleArray>(runs)
                for (run in 1..runs)
                    launch {
                        val algo = FunctionApprox(prob, PI)
                        algo.episodes = episodes
                        val _errors = DoubleArray(episodes) { 0.0 }
                        val func = LinearFunc(SimpleTileCoding(numOfTiling,
                                                               5,
                                                               ceil(prob.states.size / 5.0).toInt(),
                                                               4.0))
                        algo.α = alpha / numOfTiling
                        algo.episodeListener = { episode, _ ->
                            _errors[episode - 1] += RMS(func, trans)
                        }
                        algo.`Gradient Monte Carlo algorithm`(func, trans)
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
            }

            runBlocking {
                val trans = { s: State -> tuple2(doubleArrayOf((s as IndexedState)[0] * 5.0 / num_states), intArrayOf()) }
                for (numOfTiling in numOfTilings)
                    launch {
                        val runChan = Channel<DoubleArray>(runs)
                        for (run in 1..runs)
                            launch {
                                val algo = FunctionApprox(prob, PI)
                                algo.episodes = episodes
                                val _errors = DoubleArray(episodes) { 0.0 }
                                val func = LinearFunc(SuttonTileCoding(5,
                                                                       numOfTiling))
                                algo.α = alpha / numOfTiling
                                algo.episodeListener = { episode, _ ->
                                    _errors[episode - 1] += RMS(func, trans)
                                }
                                algo.`Gradient Monte Carlo algorithm`(func, trans)
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
                                                 domain, 50)
                val trans = { s: State -> (s as WaveState).x }
                val func = LinearFunc(feature)
                repeat(numOfSample) {
                    val (s, y) = sample()
                    func.w += alpha / feature.features.sumBy { if (it.contains(trans(s))) 1 else 0 } * (y - func(trans(s))) * func.`▽`(trans(s))
                }
                for (i in 0 until maxResolution) {
                    val s = WaveState(i * 2.0 / maxResolution)
                    val y = func(trans(s))
                    line[i * 2.0 / maxResolution] = y
                }
                chart += line
            }
            ChartView.charts += chart
        }
        Application.launch(ChartApp::class.java)
    }

    class `Mountain Car Problem` {
        @Test
        fun `Episodic Semi-gradient Sarsa control`() {
            val mdp = MountainCar.make()
            val feature = SuttonTileCoding(511, 8)
            val func = LinearFunc(feature)
            val positionScale = feature.numTilings / (POSITION_MAX - POSITION_MIN)
            val velocityScale = feature.numTilings / (VELOCITY_MAX - VELOCITY_MIN)
            val trans = { s: State, a: Action<State> ->
                s as CarState
                a as DefaultAction<Int, CarState>
                tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
            }
            val π = `ε-greedy function policy`(func, trans)
            val algo = FunctionApprox(mdp, π)
            algo.episodes = 9000
            val alpha = 0.1
            algo.α = alpha / 8
            val episodes = intArrayOf(1, 12, 104, 1000, 9000)
            algo.episodeListener = { episode, _ ->
                if (episode in episodes) {
                    val _feature = SuttonTileCoding(511, 8)
                    _feature.data.putAll(feature.data)
                    val _func = LinearFunc(_feature)
                    _func.w `=` func.w
                    val chart = D3DChart("Mountain Car", "Position", "Velocity", "Value",
                                         40, 40,
                                         POSITION_MIN..POSITION_MAX,
                                         VELOCITY_MIN..VELOCITY_MAX,
                                         0.0..120.0, 10.0, 10.0, 5.0) { x, y ->
                        if (x !in POSITION_MIN..POSITION_MAX || y !in VELOCITY_MIN..VELOCITY_MAX)
                            return@D3DChart Double.NaN
                        val f = doubleArrayOf(positionScale * x, velocityScale * y)
                        val cost = -lab.mars.rl.util.math.max(-1..1) { _func(tuple2(f, intArrayOf(it))) }
                        cost
                    }
                    charts += chart
                }
            }
            algo.`Episodic semi-gradient Sarsa control`(func, trans)

            Application.launch(D3DChartUI::class.java)
        }

        @Test
        fun `Learning curves Episodic Semi-gradient Sarsa control`() {
            logLevel(Level.ERROR)
            val mdp = MountainCar.make()

            val numTilings = 8
            val positionScale = numTilings / (POSITION_MAX - POSITION_MIN)
            val velocityScale = numTilings / (VELOCITY_MAX - VELOCITY_MIN)
            val episodes = 500
            val runs = 10
            val alphas = listOf(0.1, 0.2, 0.5)

            val chart = chart("Learning curves")
            val outerChan = Channel<Boolean>(alphas.size)
            runBlocking {
                for (alpha in alphas)
                    launch {
                        val runChan = Channel<IntArray>(runs)
                        repeat(runs) {
                            launch {
                                val feature = SuttonTileCoding(511, numTilings)
                                val func = LinearFunc(feature)

                                val trans = { s: State, a: Action<State> ->
                                    s as CarState
                                    a as DefaultAction<Int, CarState>
                                    tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
                                }
                                val π = `ε-greedy function policy`(func, trans)
                                val algo = FunctionApprox(mdp, π)
                                algo.episodes = episodes
                                algo.α = alpha / numTilings
                                val steps = IntArray(episodes)
                                algo.episodeListener = { episode, step ->
                                    steps[episode - 1] += step
                                }
                                algo.`Episodic semi-gradient Sarsa control`(func, trans)
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
            ChartView.charts += chart
            Application.launch(ChartApp::class.java)
        }

        @Test
        fun `One-step vs multi-step performance of n-step semi-gradient Sarsa`() {
            logLevel(Level.ERROR)
            val mdp = MountainCar.make()

            val numTilings = 8
            val positionScale = numTilings / (POSITION_MAX - POSITION_MIN)
            val velocityScale = numTilings / (VELOCITY_MAX - VELOCITY_MIN)
            val episodes = 500
            val runs = 10
            val alphas = listOf(0.5, 0.3)
            val nSteps = listOf(1, 8)

            val chart = chart("performance")
            val outerChan = Channel<Boolean>(nSteps.size)
            runBlocking {
                for ((i, n) in nSteps.withIndex())
                    launch {
                        val runChan = Channel<IntArray>(runs)
                        repeat(runs) {
                            launch {
                                val feature = SuttonTileCoding(511, numTilings)
                                val func = LinearFunc(feature)

                                val trans = { s: State, a: Action<State> ->
                                    s as CarState
                                    a as DefaultAction<Int, CarState>
                                    tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
                                }
                                val π = `ε-greedy function policy`(func, trans)
                                val algo = FunctionApprox(mdp, π)
                                algo.episodes = episodes
                                algo.α = alphas[i] / numTilings
                                val steps = IntArray(episodes)
                                algo.episodeListener = { episode, step ->
                                    steps[episode - 1] += step
                                }
                                algo.`Episodic semi-gradient n-step Sarsa control`(func, trans, n)
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
            ChartView.charts += chart
            Application.launch(ChartApp::class.java)
        }

        @Test
        fun `Effect of the α and n on early performance`() {
            logLevel(Level.ERROR)
            val mdp = MountainCar.make()

            val numTilings = 8
            val positionScale = numTilings / (POSITION_MAX - POSITION_MIN)
            val velocityScale = numTilings / (VELOCITY_MAX - VELOCITY_MIN)
            val episodes = 50
            val runs = 2
            val alphas = listOf(0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6)
            val nSteps = listOf(1, 2, 4, 8, 16)

            val chart = chart("performance")
            runBlocking {
                for (n in nSteps)
                    for (alpha in alphas) {
                        val runChan = Channel<IntArray>(runs)
                        repeat(runs) {
                            launch {
                                val feature = SuttonTileCoding(255, numTilings)
                                val func = LinearFunc(feature)

                                val trans = { s: State, a: Action<State> ->
                                    s as CarState
                                    a as DefaultAction<Int, CarState>
                                    tuple2(doubleArrayOf(positionScale * s.position, velocityScale * s.velocity), intArrayOf(a.value))
                                }
                                val π = `ε-greedy function policy`(func, trans)
                                val algo = FunctionApprox(mdp, π)
                                algo.episodes = episodes

                                algo.α = alpha / numTilings
                                val steps = IntArray(episodes)
                                algo.episodeListener = { episode, step ->
                                    steps[episode - 1] += step
                                }
                                algo.`Episodic semi-gradient n-step Sarsa control`(func, trans, n)
                                runChan.send(steps)
                            }
                        }
                        val steps = IntArray(episodes)
                        repeat(runs) {
                            val _steps = runChan.receive()
                            _steps.forEachIndexed { episode, s ->
                                steps[episode] += s
                            }
                            println("alpha=$alpha n=$n run once")
                        }
                        val line = line("alpha=$alpha n=$n ")
                        for (episode in 1..episodes) {
                            line[episode] = steps[episode - 1] / runs.toDouble()
                        }
                        chart += line
                        println("finish alpha=$alpha n=$n")
                    }
            }
            ChartView.charts += chart
            Application.launch(ChartApp::class.java)
        }
    }
}