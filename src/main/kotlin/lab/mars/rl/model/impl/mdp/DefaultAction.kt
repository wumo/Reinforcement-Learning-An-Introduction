package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.*

class DefaultAction<out E, out S: State>(val value: E, override val sample: () -> Possible<S>): Action<S>