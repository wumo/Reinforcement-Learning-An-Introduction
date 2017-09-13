package lab.mars.rl.model.impl

import lab.mars.rl.algo.PolicyIteration
import lab.mars.rl.algo.ValueIteration
import lab.mars.rl.problem.CarRental
import lab.mars.rl.problem.GridWorld
import org.junit.Test

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
class TestProblems {
    private fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)
    @Test
    fun `GridWorld`() {
        val prob = GridWorld.make()
        val algo = PolicyIteration(prob)
        val V = algo.v_iteration()
        for (s in prob.states) {
            println(V[s])
        }
    }

    @Test
    fun `Car Rental Policy Iteration Value`() {
        val prob = CarRental.make(false)
        val algo = PolicyIteration(prob)
        val V = algo.v_iteration()
        for (a in CarRental.max_car downTo 0) {
            for (b in 0..CarRental.max_car)
                print("" + V[prob.states[a, b]].format(2) + " ")
            println()
        }
    }

    @Test
    fun `Car Rental  Value Iteration`() {
        val prob = CarRental.make(false)
        val algo = ValueIteration(prob)
        val V = algo.iteration()
        for (a in CarRental.max_car downTo 0) {
            for (b in 0..CarRental.max_car)
                print("" + V[prob.states[a, b]].format(2) + " ")
            println()
        }
    }
}