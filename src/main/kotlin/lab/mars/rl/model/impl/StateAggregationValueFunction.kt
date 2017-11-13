package lab.mars.rl.model.impl

import lab.mars.rl.model.State
import lab.mars.rl.model.ValueFunction
import org.apache.commons.math3.util.FastMath.ceil

class StateAggregationValueFunction(numStates: Int, numOfGroups: Int) : ValueFunction {
    val w = DoubleArray(numOfGroups) { 0.0 }
    val groupSize = ceil(numStates.toDouble() / numOfGroups).toInt()
    override fun get(s: State): Double {
        if (s.isTerminal()) return 0.0
        val groupIdx = s[0] / groupSize
        return w[groupIdx]
    }

    override fun update(s: State, delta: Double) {
        if (s.isTerminal()) return
        val groupIdx = s[0] / groupSize
        w[groupIdx] += delta
    }

}