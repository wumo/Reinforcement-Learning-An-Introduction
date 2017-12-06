package lab.mars.rl.algo.dp

import lab.mars.rl.problem.CarRental
import lab.mars.rl.problem.GridWorld
import lab.mars.rl.util.format
import org.junit.Assert
import org.junit.Test

class `Test Value Iteration` {
  @Test
  fun `GridWorld Problem`() {
    val prob = GridWorld.make()
    val V = prob.ValueIteration()
    for (s in prob.states) {
      println(V[s])
    }
  }
  
  @Test
  fun `Car Rental  Value Iteration`() {
    val prob = CarRental.make(false)
    val V = prob.ValueIteration()
    var i = 0
    for (a in CarRental.max_car downTo 0)
      for (b in 0..CarRental.max_car)
        Assert.assertEquals(`Car Rental Result`[i++], V[prob.states[a, b]].format(2))
  }
}
