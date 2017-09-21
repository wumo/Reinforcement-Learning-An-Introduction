package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.NSetMDP
import lab.mars.rl.util.emptyNSet
import lab.mars.rl.util.x
import lab.mars.rl.util.nsetFrom

/**
 * <p>
 * Created on 2017-09-05.
 * </p>
 *
 * @author wumo
 */
object GridWorld {
    private const val n = 4
    private const val m = 4
    private val move = arrayOf(
            intArrayOf(-1, 0), //up
            intArrayOf(1, 0), //down
            intArrayOf(0, 1), //right
            intArrayOf(0, -1)//left
    )
    val desc_move = arrayOf(" ↑", " ↓", "→", "←")
    fun make(): MDP {
        val mdp = NSetMDP(gamma = 0.9, // 因为我们使用的是确定策略，但是GridWorld问题中存在确定策略的无限循环，此时便不是episode mdp，gamma必须小于1
                          state_dim = n x n,
                          action_dim = m)
        mdp.apply {
            for (s in states)
                for (action in s.actions) {
                    var x = s[0] + move[action[0]][0]
                    var y = s[1] + move[action[0]][1]
                    if (x < 0 || x >= n || y < 0 || y >= n) {
                        x = s[0]
                        y = s[1]
                    }
                    action.possibles = nsetFrom(1) { Possible(states[x, y], -1.0, 1.0) }
                }
            states[0, 0].actions = emptyNSet()
            states[n - 1, n - 1].actions =  emptyNSet()
        }

        return mdp
    }
}