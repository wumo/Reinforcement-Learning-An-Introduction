package lab.mars.rl.problem

import lab.mars.rl.model.MDP
import lab.mars.rl.model.Possible
import lab.mars.rl.model.State
import lab.mars.rl.model.impl.CNSetMDP
import lab.mars.rl.util.dimension.x
import lab.mars.rl.util.emptyNSet

object WindyGridwold {
    val world_height = 7
    val world_width = 10
    val wind = intArrayOf(0, 0, 0, 1, 1, 1, 2, 2, 1, 0)//wind strength for each column
    val move = arrayOf(
            intArrayOf(-1, 0), //up
            intArrayOf(1, 0), //down
            intArrayOf(0, 1), //right
            intArrayOf(0, -1)//left
    )
    lateinit var goal: State

    fun make(): MDP {
        val mdp = CNSetMDP(gamma = 0.9, // 因为我们使用的是确定策略，但是GridWorld问题中存在确定策略的无限循环，此时便不是episode mdp，gamma必须小于1
                           state_dim = world_width x world_height,
                           action_dim = 4)
        return mdp.apply {
            goal = states[7, 3]
            goal.actions = emptyNSet()
            started = states(0, 3)
            for (s in states) {
                if (s.isTerminal()) continue
                for (a in s.actions) {
                    a.sample = {
                        val m = move[a[0]]
                        val x = (s[0] + m[0]).coerceIn(0, world_width - 1)
                        val y = (s[1] + wind[s[0]] + m[1]).coerceIn(0, world_height - 1)
                        val next = states[x, y]
                        Possible(next, if (next === goal) 0.0 else -1.0, 1.0)
                    }
                }
            }
        }
    }
}