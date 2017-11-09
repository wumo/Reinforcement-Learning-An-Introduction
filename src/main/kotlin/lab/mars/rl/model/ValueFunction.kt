package lab.mars.rl.model

interface ValueFunction {
    operator fun get(s: State): Double
    fun update(s: State, target: Double,alpha:Double)
}