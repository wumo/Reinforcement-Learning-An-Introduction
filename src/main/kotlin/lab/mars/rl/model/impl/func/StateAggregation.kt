package lab.mars.rl.model.impl.func

import lab.mars.rl.model.ApproximateFunction
import lab.mars.rl.util.matrix.Matrix
import org.apache.commons.math3.util.FastMath.ceil

class StateAggregation(numStates: Int, val numOfGroups: Int, conv: (Array<out Any>) -> Int) : ApproximateFunction<Int>(conv) {
  override fun `_▽`(input: Int): Matrix {
    val groupIdx = input / groupSize
    return Matrix.column(numOfGroups) { if (it == groupIdx) 1.0 else 0.0 }
  }

  override val w = Matrix.column(numOfGroups) { 0.0 }
  val groupSize = ceil(numStates.toDouble() / numOfGroups).toInt()

  override fun _invoke(input: Int): Double {
    val groupIdx = input / groupSize
    return w[groupIdx]
  }
}