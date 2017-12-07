package lab.mars.rl.problem

import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.dimension.cnsetFrom
import lab.mars.rl.util.dimension.x
import lab.mars.rl.util.math.*

object AccessControl {
  val k = 10
  val p = 0.06
  val priorities = 0..3
  val rewards = pow(2.0, priorities)
  val reject = 0
  val accept = 1
  fun make(): IndexedMDP {
    val mdp = CNSetMDP(gamma = 0.9, state_dim = (k + 1) x 4, action_dim = { (fs) ->
      if (fs == 0) 1 else 2
    })
    
    return mdp.apply {
      started = { states[k, Rand().nextInt(4)] }
      for (s in states) {
        var (freeServers, priority) = s
        for (a in s.actions) {
          var reward = 0.0
          if (freeServers > 0 && a[0] == accept) {
            freeServers--
            reward = rewards[priority]
          }
          val busyServers = k - freeServers
          a.possibles = cnsetFrom((busyServers + 1) x 4) { (released, pr) ->
            IndexedPossible(states[freeServers + released, pr],
                            reward,
                            binomial(busyServers, released, p) * (1 / 4.0))
          }
        }
      }
    }
  }
}