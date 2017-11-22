package lab.mars.rl.problem

import lab.mars.rl.model.impl.mdp.IndexedMDP
import lab.mars.rl.model.impl.mdp.IndexedPossible
import lab.mars.rl.model.impl.mdp.CNSetMDP
import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.buf.IntBuf
import lab.mars.rl.util.collection.cnsetOf
import lab.mars.rl.util.dimension.x
import lab.mars.rl.util.collection.emptyNSet

object DynaMaze {
    private val move = arrayOf(
            intArrayOf(-1, 0), //left
            intArrayOf(1, 0), //right
            intArrayOf(0, 1), //up
            intArrayOf(0, -1)//down
    )
    val desc_move = arrayOf("←", "→", " ↑", " ↓")
    val wall = hashSetOf<IntBuf>()
    val obstacle = hashSetOf<IntBuf>()

    init {
        for (x in -1..9) {
            wall += DefaultIntBuf.of(x, -1)
            wall += DefaultIntBuf.of(x, 6)
        }
        for (y in -1..6) {
            wall += DefaultIntBuf.of(-1, y)
            wall += DefaultIntBuf.of(9, y)
        }
        obstacle += DefaultIntBuf.of(2, 2)
        obstacle += DefaultIntBuf.of(2, 3)
        obstacle += DefaultIntBuf.of(2, 4)

        obstacle += DefaultIntBuf.of(5, 1)

        obstacle += DefaultIntBuf.of(7, 3)
        obstacle += DefaultIntBuf.of(7, 4)
        obstacle += DefaultIntBuf.of(7, 5)

        wall += obstacle
    }

    fun make(): IndexedMDP {
        val mdp = CNSetMDP(gamma = 0.95,
                                                      state_dim = 9 x 6,
                                                      action_dim = 4)
        return mdp.apply {
            for (s in states)
                for (action in s.actions) {
                    val tmp = DefaultIntBuf.of(0, 0)
                    tmp[0] = s[0] + move[action[0]][0]
                    tmp[1] = s[1] + move[action[0]][1]
                    if (tmp in wall) {
                        tmp[0] = s[0]
                        tmp[1] = s[1]
                    }
                    val reward = if (tmp[0] == 8 && tmp[1] == 5) 1.0 else 0.0
                    action.possibles = cnsetOf(IndexedPossible(states[tmp], reward, 1.0))

                }
            states[8, 5].actions = emptyNSet()
            for (o in obstacle)
                states[o].actions = emptyNSet()
            started = states(0, 3)
        }

    }
}