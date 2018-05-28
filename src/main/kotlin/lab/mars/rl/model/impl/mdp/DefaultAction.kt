package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.Action
import lab.mars.rl.model.Possible
import lab.mars.rl.model.State

class DefaultAction<out E, out S: State>(val value: E, override val sample: () -> Possible<S>): Action<S>