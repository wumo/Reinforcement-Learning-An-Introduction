package lab.mars.rl.algo.dyna

import lab.mars.rl.model.Action
import lab.mars.rl.model.ActionValueFunction
import lab.mars.rl.model.State
import lab.mars.rl.util.*
import org.slf4j.LoggerFactory

interface Environment {
    fun current(): State
    fun act(a: Action): tuple2<Double, State>
}

@Suppress("NAME_SHADOWING")
class DynaQ(val environment: Environment, val Q: ActionValueFunction, val Model: RandomAccessCollection<tuple4<State, Action, Double, State>>) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val current = environment::current
    val act = environment::act

    val gamma = 0.9
    var episodes = 10000
    var epsilon = 0.1
    var alpha = 0.1
    var n = 10

    fun start(_alpha: (State, Action) -> Double = { _, _ -> alpha }) {
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            val s = current()
            val a = if (Rand().nextDouble() < epsilon) s.actions.rand()
            else argmax(s.actions) { Q[s, it] }
            val (reward, s_next) = act(a)
            Q[s, a] += _alpha(s, a) * (reward + gamma * max(s_next.actions) { Q[s_next, it] } - Q[s, a])
            Model[s, a] = tuple4(s, a, reward, s_next)
            repeat(n) {
                val (s, a, reward, s_next) = Model.rand()
                Q[s, a] += _alpha(s, a) * (reward + gamma * max(s_next.actions) { Q[s_next, it] } - Q[s, a])
            }
        }
    }
}