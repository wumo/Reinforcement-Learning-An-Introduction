package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.MDP
import lab.mars.rl.model.State

class DefaultMDP(override val γ: Double, override val started: () -> State) : MDP