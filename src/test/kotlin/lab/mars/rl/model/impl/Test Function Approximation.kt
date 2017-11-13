package lab.mars.rl.model.impl

import javafx.application.Application
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.`Gradient Monte Carlo algorithm`
import lab.mars.rl.algo.func_approx.`Semi-gradient TD(0)`
import lab.mars.rl.algo.func_approx.`n-step semi-gradient TD`
import lab.mars.rl.algo.td.TemporalDifference
import lab.mars.rl.algo.td.prediction
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.problem.`1000-state RandomWalk`.make
import lab.mars.rl.problem.`1000-state RandomWalk`.num_states
import lab.mars.rl.util.ui.Chart
import lab.mars.rl.util.ui.ChartView
import org.apache.commons.math3.util.FastMath.pow
import org.apache.commons.math3.util.FastMath.sqrt
import org.junit.Test

class `Test Function Approximation` {
    class `1000-state Random walk problem` {
        @Test
        fun `Gradient Monte Carlo`() {
            val (prob, PI) = make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 100000
            val V = algo.prediction()
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${V[s].format(2)} ")
                    line.put(s[0], V[s])
                }
                ChartView.lines += Pair("TD", line)
            }

            val algo2 = FunctionApprox(prob, PI)
            algo2.episodes = 100000
            algo2.alpha = 2e-5
            val func = StateAggregationValueFunction(num_states + 2, 10)
            algo2.`Gradient Monte Carlo algorithm`(func)
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${func[s].format(2)} ")
                    line.put(s[0], func[s])
                }
                ChartView.lines += Pair("gradient MC", line)
            }
            Application.launch(Chart::class.java)
        }

        @Test
        fun `Semi-gradient TD(0)`() {
            val (prob, PI) = make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 100000
            val V = algo.prediction()
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${V[s].format(2)} ")
                    line.put(s[0], V[s])
                }
                ChartView.lines += Pair("TD", line)
            }

            val algo2 = FunctionApprox(prob, PI)
            algo2.episodes = 100000
            algo2.alpha = 2e-4
            val func = StateAggregationValueFunction(num_states + 2, 10)
            algo2.`Semi-gradient TD(0)`(func)
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${func[s].format(2)} ")
                    line.put(s[0], func[s])
                }
                ChartView.lines += Pair("Semi-gradient TD(0)", line)
            }
            Application.launch(Chart::class.java)
        }

        @Test
        fun `n-step semi-gradient TD`() {
            val (prob, PI) = make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 100000
            val V = algo.prediction()
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${V[s].format(2)} ")
                    line.put(s[0], V[s])
                }
                ChartView.lines += Pair("TD", line)
            }

            val algo2 = FunctionApprox(prob, PI)
            algo2.episodes = 100000
            algo2.alpha = 2e-4
            val func = StateAggregationValueFunction(num_states + 2, 10)
            algo2.`n-step semi-gradient TD`(10, func)
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${func[s].format(2)} ")
                    line.put(s[0], func[s])
                }
                ChartView.lines += Pair("n-step semi-gradient TD", line)
            }
            Application.launch(Chart::class.java)
        }

        @Test
        fun `Gradient Monte Carlo with Fourier basis vs polynomials`() {

            val (prob, PI) = make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 100000
            val V = algo.prediction()

            fun RMS(f: ValueFunction): Double {
                var result = 0.0
                for (s in prob.states)
                    result += pow(V[s] - f[s], 2)
                result /= prob.states.size
                return sqrt(result)
            }

            val orders = intArrayOf(5, 10, 20)
            val algo2 = FunctionApprox(prob, PI)
            algo2.episodes = 5000
            val errors = DoubleArray(algo2.episodes) { 0.0 }
            val run = 100
            //polynomials
            algo2.alpha = 1e-4
            var remains = run * orders.size * 2
            for (order in orders) {
                for (i in 1..run) {
                    remains--
                    println("run: $i polynomial order=$order")
                    val last = System.currentTimeMillis()
                    val func = LinearFunc(SimplePolynomial(order + 1, 1.0 / num_states))
                    algo2.episodeListener = { episode ->
                        errors[episode - 1] += RMS(func)
                    }
                    algo2.`Gradient Monte Carlo algorithm`(func)
                    val d = (System.currentTimeMillis() - last) / 1000.0
                    println("${d.format(2)}s, ETA: ${(d * remains / 60).format(2)}min")
                }

                val line = hashMapOf<Number, Number>()
                for (episode in 1..algo2.episodes) {
                    line.put(episode, errors[episode - 1] / run)
                    errors[episode - 1] = 0.0
                }
                ChartView.lines += Pair("polynomial order=$order", line)
            }

            for (order in orders) {
                for (i in 1..run) {
                    remains--
                    println("run: $i fourier order=$order")
                    val last = System.currentTimeMillis()
                    val func = LinearFunc(SimpleFourier(order + 1, 1.0 / num_states))
                    algo2.episodeListener = { episode ->
                        errors[episode - 1] += RMS(func)
                    }
                    algo2.`Gradient Monte Carlo algorithm`(func)
                    val d = (System.currentTimeMillis() - last) / 1000.0
                    println("${d.format(2)}s, ETA: ${(d * remains / 60).format(2)}min")
                }

                val line = hashMapOf<Number, Number>()
                for (episode in 1..algo2.episodes) {
                    line.put(episode, errors[episode - 1] / run)
                    errors[episode - 1] = 0.0
                }
                ChartView.lines += Pair("fourier order=$order", line)
            }

//            //fourier
//            algo2.alpha = 5e-5
//            for (order in orders) {
//                println("fourier order=$order")
//                val func = LinearFunc(SimpleFourier(order + 1, 1.0 / num_states))
//                algo2.`Gradient Monte Carlo algorithm`(func)
//                prob.apply {
//                    val line = hashMapOf<Number, Number>()
//                    for (s in states) {
//                        println("${func[s].format(2)} ")
//                        line.put(s[0], func[s])
//                    }
//                    ChartView.lines += line
//                }
//            }

            Application.launch(Chart::class.java)
        }
    }
}