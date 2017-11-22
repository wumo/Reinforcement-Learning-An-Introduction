package lab.mars.rl.model

import lab.mars.rl.util.collection.Gettable
import lab.mars.rl.util.matrix.Matrix

interface ActionValueApproxFunction : Gettable<Action<State>, Double> {
    val w: Matrix
    /**
     * @return v(S,w)
     */
    operator fun invoke(s: State, a: Action<State>): Double

    /**
     * @param s current state
     * @param delta `α*[Gt-v(S,w)]`, do not multiply `∇`
     */
    fun update(s: State, a: Action<State>, delta: Double)

    fun `▽`(s: State, a: Action<State>): Matrix
}