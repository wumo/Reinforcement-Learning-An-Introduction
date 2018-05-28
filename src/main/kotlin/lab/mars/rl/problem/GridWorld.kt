package lab.mars.rl.problem

import lab.mars.rl.model.impl.mdp.CNSetMDP
import lab.mars.rl.model.impl.mdp.IndexedMDP
import lab.mars.rl.model.impl.mdp.IndexedPossible
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.collection.cnsetOf
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.collection.filter
import lab.mars.rl.util.collection.fork
import lab.mars.rl.util.dimension.x

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
  fun make(): IndexedMDP {
    val mdp = CNSetMDP(gamma = 0.9,
                       state_dim = n x n,
                       action_dim = m)
    mdp.apply {
      for ((s, action) in states.filter { it.isNotTerminal }.fork { it.actions }) {
        val (s0, s1) = s
        val (a) = action
        var x = s0 + move[a][0]
        var y = s1 + move[a][1]
        if (x < 0 || x >= n || y < 0 || y >= n) {
          x = s0
          y = s1
        }
        action.possibles = cnsetOf(IndexedPossible(states[x, y], -1.0, 1.0))
      }
      states[0, 0].actions = emptyNSet()
      states[n - 1, n - 1].actions = emptyNSet()
    }
    
    return mdp
  }
}