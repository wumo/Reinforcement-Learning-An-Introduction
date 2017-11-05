package lab.mars.rl.problem

import lab.mars.rl.model.MDP
import lab.mars.rl.model.Possible
import lab.mars.rl.model.impl.CNSetMDP
import lab.mars.rl.util.cnsetOf
import lab.mars.rl.util.dimension.x
import lab.mars.rl.util.emptyNSet

object WindyGridworld {
    val world_height = 7
    val world_width = 10
    val wind = intArrayOf(0, 0, 0, 1, 1, 1, 2, 2, 1, 0)//wind strength for each column
    val move = arrayOf(
            intArrayOf(0, 1), //up
            intArrayOf(0, -1), //down
            intArrayOf(-1, 0), //left
            intArrayOf(1, 0)//right
    )
    val kingMove = arrayOf(
            intArrayOf(0, 1), //up
            intArrayOf(0, -1), //down
            intArrayOf(-1, 0), //left
            intArrayOf(1, 0),//right
            intArrayOf(-1, 1), //up
            intArrayOf(1, 1), //down
            intArrayOf(1, -1), //left
            intArrayOf(-1, -1)//right
    )
    val desc_move = arrayOf(" ↑", " ↓", "←", "→")
    val desc_king_move = arrayOf(" ↑", " ↓", "←", "→", "↖", "↗", "↘", "↙")
    fun make(KingMove: Boolean = false): MDP {
        val mdp = CNSetMDP(gamma = 1.0,
                           state_dim = world_width x world_height,
                           action_dim = if (KingMove) 8 else 4)
        return mdp.apply {
            val goal = states[7, 3]
            goal.actions = emptyNSet()
            started = states(0, 3)
            for (s in states) {
                if (s.isTerminal()) continue
                for (a in s.actions) {
                    val m = (if (KingMove) kingMove else move)[a[0]]
                    val x = (s[0] + m[0]).coerceIn(0, world_width - 1)
                    val y = (s[1] + wind[s[0]] + m[1]).coerceIn(0, world_height - 1)
                    val next = states[x, y]
                    a.possibles = cnsetOf(Possible(next, if (next === goal) 0.0 else -1.0, 1.0))
                }
            }
        }
    }
}