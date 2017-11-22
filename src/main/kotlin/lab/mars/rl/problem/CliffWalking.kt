package lab.mars.rl.problem

import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isTerminal
import lab.mars.rl.util.collection.cnsetOf
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.dimension.x

object CliffWalking {
    val world_height = 4
    val world_width = 12
    val move = arrayOf(
            intArrayOf(0, 1), //up
            intArrayOf(0, -1), //down
            intArrayOf(-1, 0), //left
            intArrayOf(1, 0)//right
    )
    val desc_move = arrayOf(" ↑", " ↓", "←", "→")
    fun make(): IndexedMDP {
        val mdp = CNSetMDP(gamma = 1.0,
                                                      state_dim = world_width x world_height,
                                                      action_dim = 4)
        return mdp.apply {
            val goal = states[11, 0]
            goal.actions = emptyNSet()
            started = states(0, 0)
            val startedState = states[0, 0]

            //cliff
            for (x in 1 until world_width - 1)
                states[x, 0].actions = emptyNSet()

            for (s in states) {
                if (s.isTerminal()) continue
                for (a in s.actions) {
                    val m = move[a[0]]
                    val _x = (s[0] + m[0]).coerceIn(0, world_width - 1)
                    val _y = (s[1] + +m[1]).coerceIn(0, world_height - 1)
                    val next = states[_x, _y]
                    a.possibles = cnsetOf(IndexedPossible(next, if (next === goal) 0.0 else -1.0, 1.0))
                }
            }
            startedState.actions[3].possibles = cnsetOf(IndexedPossible(startedState, -100.0, 1.0))
            for (x in 1 until world_width - 1) {
                val s = states[x, 1]
                for (a in s.actions) {
                    val m = move[a[0]]
                    var _x = s[0] + m[0]
                    var _y = s[1] + +m[1]
                    if (_y == 0) {
                        _x = 0
                        _y = 0
                    }
                    val next = states[_x, _y]
                    a.possibles = cnsetOf(IndexedPossible(next, if (next === startedState) -100.0 else -1.0, 1.0))
                }
            }

        }
    }
}