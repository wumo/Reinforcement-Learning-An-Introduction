package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.problem.MountainCar.POSITION_MAX
import lab.mars.rl.problem.MountainCar.POSITION_MIN
import lab.mars.rl.problem.MountainCar.VELOCITY_MAX
import lab.mars.rl.problem.MountainCar.VELOCITY_MIN
import org.apache.commons.math3.util.FastMath.cos

class CarState(val position: Double, val velocity: Double) : State {
    override val actions: Iterable<Action<CarState>> =
            ArrayList<Action<CarState>>(3).apply {
                for (action in -1..1)
                    this += DefaultAction(
                            {
                                val newVelocity = (velocity + 0.001 * action - 0.0025 * cos(3 * position))
                                        .coerceIn(VELOCITY_MIN, VELOCITY_MAX)
                                val newPosition = (position + newVelocity).coerceIn(POSITION_MIN, POSITION_MAX)
                                Possible(CarState(newPosition, newVelocity), -1.0)
                            })
            }
}

object MountainCar {
    val POSITION_MIN = -1.2
    val POSITION_MAX = 0.5
    val VELOCITY_MIN = -0.07
    val VELOCITY_MAX = 0.07
    fun make(): MDP {
        TODO()
    }
}