package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.NSet
import lab.mars.rl.model.impl.NSetMDP

/**
 * <p>
 * Created on 2017-09-05.
 * </p>
 *
 * @author wumo
 */
object GridWorld {
    private const val n = 4
    private const val action_num = 4
    private val move = arrayOf(
            intArrayOf(-1, 0), //up
            intArrayOf(1, 0), //down
            intArrayOf(0, 1), //right
            intArrayOf(0, -1)//left
    )
    val desc_move = arrayOf(" ↑", " ↓", "→", "←")
    fun make(): MDP {
        val mdp = NSetMDP(gamma = 0.9,
                state_dim = intArrayOf(n, n), action_dim = intArrayOf(action_num))// 因为我们使用的是确定策略，但是GridWorld问题中存在确定策略的无限循环，此时便不是episode mdp，gamma必须小于1
        mdp.apply {
            for (s in states) {
                s!!.actions = NSet(action_num) {
                    val action_idx = it[0]
                    val action = Action(action_idx)
                    var x = s.idx[0] + move[action_idx][0]
                    var y = s.idx[1] + move[action_idx][1]
                    if (x < 0 || x >= n || y < 0 || y >= n) {
                        x = s.idx[0]
                        y = s.idx[1]
                    }
                    action.possibles = NSet(1) { Possible(states[x, y]!!, -1.0, 1.0) }
                    action
                }
            }
            states[0, 0]!!.actions = emptyActions
            states[n - 1, n - 1]!!.actions = emptyActions
        }

        return mdp
    }
}