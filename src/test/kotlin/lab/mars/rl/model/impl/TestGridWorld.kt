package lab.mars.rl.model.impl

import lab.mars.rl.algo.PolicyIteration
import lab.mars.rl.problem.GridWorld
import org.junit.Test

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
class TestGridWorld {
    @Test
    fun `Test`() {
        val prob = GridWorld.make()
        val algo = PolicyIteration(prob)
        val V = algo.v_iteration()
        for (s in prob.states) {
            println(V[s!!])
        }
    }

}