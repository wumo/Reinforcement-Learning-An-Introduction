package lab.mars.rl.model.impl

import javafx.application.Application
import lab.mars.rl.algo.approximation.`Gradient Monte Carlo algorithm`
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
            val algo = `Gradient Monte Carlo algorithm`(prob, PI)
            algo.episodes = 100000
            val func = StateAggregationValueFunction(num_states + 2, 10)
            algo.prediction(func)
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${func[s].format(2)} ")
                    line.put(s[0], func[s])
                }
                ChartView.lines += line
            }

            val algo2 = TemporalDifference(prob, PI)
            algo2.episodes = 100000
            val V = algo2.prediction()
            prob.apply {
                val line = hashMapOf<Number, Number>()
                for (s in states) {
                    println("${V[s].format(2)} ")
                    line.put(s[0], V[s])
                }
                ChartView.lines += line
            }
            Application.launch(Chart::class.java)
        }
    }
}