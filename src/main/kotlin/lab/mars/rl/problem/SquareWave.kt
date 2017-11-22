package lab.mars.rl.problem

import lab.mars.rl.model.ActionSet
import lab.mars.rl.model.State
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.tuples.tuple2

class D1DState(val x: Double) : State {
    override var actions: ActionSet = emptyNSet()
}

object SquareWave {
    val domain = 0.0..2.0
    val maxResolution = 100
    fun invoke(x: Double) = if (x in 0.5..1.5) 1.0 else 0.0
    fun sample(): tuple2<State, Double> {
        val x = Rand().nextDouble(domain.start, domain.endInclusive)
        val y = invoke(x)
        return tuple2(D1DState(x), y)
    }
}