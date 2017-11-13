package lab.mars.rl.model

interface ValueFunction {
    /**
     * @return v(S,w)
     */
    operator fun get(s: State): Double

    /**
     * @param s current state
     * @param delta `α*[Gt-v(S,w)]`, do not multiply gradient
     */
    fun update(s: State, delta: Double)
}