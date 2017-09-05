package lab.mars.rl.model.impl

import io.kotlintest.specs.StringSpec
import lab.mars.rl.model.Action
import lab.mars.rl.model.MDP
import lab.mars.rl.model.State

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
class TestDimNSet : StringSpec() {
    init {
        "inti raw with correct index and 0"{
            var i = 0
            var set = DimNSet<Int?>(3, 4, 5) { idx -> println(idx.toList()); i++ }
//            set.forEach { println(it) }
//            println(set[2, 3, 4])
            set[2, 3, 4] = 100
//            println(set[2, 3, 4])
            set = DimNSet(3, 4, 5)
            set[0, 0, 0] = 1
//            println(set[0, 0, 0])
        }

        "DimNSetMDP" {
            val mdp = DimNSetMDP(state_dim = intArrayOf(3, 4, 5), action_dim = intArrayOf(4))
            val S = mdp.states
            val V = mdp.v_maker()
            val PI = mdp.pi_maker()
            val Q = mdp.q_maker()
            S.init { idx ->
                println(">>${idx[0]},${idx[1]},${idx[2]}")
                val s = State(*idx)
                s.actions = DimNSet(4) { Action(*it) }
                s
            }
            val index = intArrayOf(2, 3, 4)
            S.set(*index, s = null)
            S.forEach { s ->
                if (s == null) return@forEach
                println(s)
                V[s] = 1.0
                val a = s.actions?.firstOrNull()
                PI[s] = s.actions?.first()
                if (a == null) return@forEach
                Q[s, a] = 1.0
            }
        }
    }
}

val dim = intArrayOf(2, 3, 4)
val mdp = MDP(
        states = DimNSet(*dim),
        gamma = 0.9,
        v_maker = { DimNSet(*dim) { 0.0 } },
        q_maker = { DimNSet(*dim, 4) { 0.0 } },
        pi_maker = { DimNSet(*dim, 4) })
