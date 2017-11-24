package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.DefaultMDP
import lab.mars.rl.problem.MountainCar.POSITION_MAX
import lab.mars.rl.problem.MountainCar.POSITION_MIN
import lab.mars.rl.problem.MountainCar.VELOCITY_MAX
import lab.mars.rl.problem.MountainCar.VELOCITY_MIN
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.dimension.cnsetFrom
import lab.mars.rl.util.math.Rand
import org.apache.commons.math3.util.FastMath.cos

class CarState(val position: Double, val velocity: Double) : State {
    override val actions: RandomIterable<Action<CarState>> =
            if (position == POSITION_MAX) emptyNSet()
            else cnsetFrom(3) {
                val a = it[0] - 1
                DefaultAction(a) {
                    val newVelocity = (velocity + 0.001 * a - 0.0025 * cos(3 * position))
                            .coerceIn(VELOCITY_MIN, VELOCITY_MAX)
                    val newPosition = (position + newVelocity).coerceIn(POSITION_MIN, POSITION_MAX)
                    Possible(CarState(newPosition, newVelocity), -1.0)
                }
            }
}

object MountainCar {
    const val POSITION_MIN = -1.2
    const val POSITION_MAX = 0.5
    const val VELOCITY_MIN = -0.07
    const val VELOCITY_MAX = 0.07
    fun make() = DefaultMDP(1.0) {
        CarState(Rand().nextDouble(-0.6, -0.4), 0.0)
    }
}