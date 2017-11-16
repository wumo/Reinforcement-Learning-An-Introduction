package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`ε-greedy (tie broken randomly)`
import lab.mars.rl.model.*
import lab.mars.rl.util.Rand
import lab.mars.rl.util.debug
import lab.mars.rl.util.max
import lab.mars.rl.util.repeat
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.tuples.tuple3
import org.apache.commons.math3.util.FastMath.abs
import org.slf4j.LoggerFactory
import java.util.*

@Suppress("NAME_SHADOWING")
class PrioritizedSweepingStochasticEnv(val mdp: MDP) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val γ = mdp.γ
    val started = mdp.started
    val states = mdp.states
    var stepListener: (ActionValueFunction, State) -> Unit = { _, _ -> }
    var episodeListener: (StateValueFunction) -> Unit = {}

    var episodes = 10000
    var α = 0.1
    var ε = 0.1
    var θ = 0.0
    var n = 10

    fun optimal(_alpha: (State, Action) -> Double = { _, _ -> α }): OptimalSolution {
        val π = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { 0.0 }
        val PQueue = PriorityQueue(Q.size, Comparator<tuple3<Double, State, Action>> { o1, o2 ->
            o2._1.compareTo(o1._1)
        })
        val Model = mdp.QFunc { hashMapOf<tuple2<State, Double>, Int>() }
        val N = mdp.QFunc { 0 }
        val predecessor = mdp.VFunc { hashSetOf<tuple2<State, Action>>() }
        val V = mdp.VFunc { 0.0 }
        val result = tuple3(π, V, Q)
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var step = 0
            var s = started.rand()
            while (s.isNotTerminal()) {
                V_from_Q_ND(states, result)
                stepListener(V, s)
                step++
                `ε-greedy (tie broken randomly)`(s, Q, π, ε)
                val a = s.actions.rand(π(s))
                val (s_next, reward, _) = a.sample()
                Model[s, a].compute(tuple2(s_next, reward)) { _, v -> (v ?: 0) + 1 }
                N[s, a]++
                predecessor[s_next] += tuple2(s, a)
                val P = abs(reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                if (P > θ) PQueue.add(tuple3(P, s, a))
                repeat(n, { PQueue.isNotEmpty() }) {
                    val (_, s, a) = PQueue.poll()
                    val (s_next, reward) = Model[s, a].rand(N[s, a])
                    Q[s, a] += _alpha(s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                    for ((s_pre, a_pre) in predecessor[s]) {
                        val reward = Model[s_pre, a_pre].expectedReward(s)
                        val P = abs(reward + γ * max(s.actions, 0.0) { Q[s, it] } - Q[s_pre, a_pre])
                        if (P > θ) PQueue.add(tuple3(P, s_pre, a_pre))
                    }
                }
                s = s_next
            }
            episodeListener(V)
            log.debug { "steps=$step" }
        }
        return result
    }

    fun HashMap<tuple2<State, Double>, Int>.rand(N: Int): tuple2<State, Double> {
        if (isEmpty()) throw NoSuchElementException()
        val p = Rand().nextDouble()
        var acc = 0.0
        for ((k, v) in this) {
            acc += v.toDouble() / N
            if (p <= acc)
                return k
        }
        throw IllegalArgumentException("random=$p, but accumulation=$acc")
    }

    fun HashMap<tuple2<State, Double>, Int>.expectedReward(s: State): Double {
        if (isEmpty()) throw NoSuchElementException()
        var sum = 0.0
        var N = 0
        for ((k, v) in this)
            if (k._1 == s) {
                sum += k._2 * v
                N += v
            }
        return sum / N
    }
}