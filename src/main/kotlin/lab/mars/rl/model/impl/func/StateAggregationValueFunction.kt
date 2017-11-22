package lab.mars.rl.model.impl.func

import lab.mars.rl.model.*
import lab.mars.rl.util.matrix.Matrix
import org.apache.commons.math3.util.FastMath.ceil

class StateAggregationValueFunction(numStates: Int, val numOfGroups: Int, val scalar: (State) -> Int) : ValueFunction {
    override fun `▽`(s: State): Matrix {
        val groupIdx = scalar(s) / groupSize
        return Matrix.column(numOfGroups) { if (it == groupIdx) 1.0 else 0.0 }
    }

    override val w = Matrix.column(numOfGroups) { 0.0 }
    val groupSize = ceil(numStates.toDouble() / numOfGroups).toInt()

    override fun invoke(s: State): Double {
        if (s.isTerminal()) return 0.0
        val groupIdx = scalar(s) / groupSize
        return w[groupIdx]
    }
}