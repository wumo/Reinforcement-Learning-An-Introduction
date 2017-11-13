package lab.mars.rl.model.impl

import javafx.application.Application
import lab.mars.rl.algo.func_approx.`Gradient Monte Carlo algorithm`
import lab.mars.rl.algo.func_approx.`Semi-gradient TD(0)`
import lab.mars.rl.algo.td.TemporalDifference
import lab.mars.rl.algo.td.prediction
import lab.mars.rl.problem.`1000-state RandomWalk`.make
import lab.mars.rl.problem.`1000-state RandomWalk`.num_states
import lab.mars.rl.util.ui.Chart
import lab.mars.rl.util.ui.ChartView
import org.junit.Test

class `Test On-policy prediction with Approximation` {
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
                ChartView.lines += line
            }

            val algo2 = `Gradient Monte Carlo algorithm`(prob, PI)
            algo2.episodes = 100000
            algo2.alpha = 2e-5
            val func = StateAggregationValueFunction(num_states + 2, 10)
            algo2.prediction(func)
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${func[s].format(2)} ")
                    line.put(s[0], func[s])
                }
                ChartView.lines += line
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
                ChartView.lines += line
            }

            val algo2 = `Semi-gradient TD(0)`(prob, PI)
            algo2.episodes = 100000
            algo2.alpha = 2e-4
            val func = StateAggregationValueFunction(num_states + 2, 10)
            algo2.prediction(func)
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${func[s].format(2)} ")
                    line.put(s[0], func[s])
                }
                ChartView.lines += line
            }
            Application.launch(Chart::class.java)
        }
    }
}