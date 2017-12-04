package lab.mars.rl.algo.dp

import lab.mars.rl.problem.CarRental
import lab.mars.rl.util.format
import org.junit.Assert
import org.junit.Test

class `Test Value Iteration` {
  @Test
  fun `GridWorld`() {
    val prob = lab.mars.rl.problem.GridWorld.make()
    val algo = ValueIteration(prob)
    val V = algo.iteration()
    for (s in prob.states) {
      println(V[s])
    }
  }

  @Test
  fun `Car Rental  Value Iteration`() {
    val prob = CarRental.make(false)
    val algo = ValueIteration(prob)
    val V = algo.iteration()
    var i = 0
    for (a in CarRental.max_car downTo 0)
      for (b in 0..CarRental.max_car)
        Assert.assertEquals(`Car Rental Result`[i++], V[prob.states[a, b]].format(2))
  }
}
