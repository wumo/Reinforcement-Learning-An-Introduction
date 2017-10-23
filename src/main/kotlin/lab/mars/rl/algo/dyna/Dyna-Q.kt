package lab.mars.rl.algo.dyna

import lab.mars.rl.model.*
import lab.mars.rl.util.*
import org.slf4j.LoggerFactory

interface Environment {
    fun current(): State
    fun actions(s: State): ActionSet
    fun act(a: Action): tuple2<Double, State>
}

class DynaQ(val environment: Environment) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val Q = hashMapOf<tuple2<State, Action>, Double>()
    val Model = hashMapOf<tuple2<State, Action>, tuple2<Double, State>>()
    val gamma = 0.9
    var episodes = 10000
    var epsilon = 0.1
    var alpha = 0.1
    var n = 10

    fun start(_alpha: (State, Action) -> Double = { _, _ -> alpha }) {
        val tmp = tuple2(null_state, null_action)
        val tmp2 = tuple2(null_state, null_action)
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            val s = environment.current()
            tmp.first = s
            val a: Action = if (Rand().nextDouble() < epsilon) environment.actions(s).rand()
            else argmax(environment.actions(s)) { tmp.second = it;Q[tmp] ?: 0.0 }
            val possible = environment.act(a)
            tmp2.first = possible.second
            Q[tmp] = Q[tmp] ?: 0.0 +
                     _alpha(s, a) * (possible.first + gamma * max(environment.actions(possible.second))
                     { tmp2.second = it;Q[tmp2] ?: 0.0 } - (Q[tmp] ?: 0.0))
            Model[tmp] = possible
            repeat(n) {
//                val chosen=Model.entries.rand()

            }
        }
    }
}