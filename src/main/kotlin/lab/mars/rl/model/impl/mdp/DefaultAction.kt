package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.*

class DefaultAction<S : State>(override val sample: () -> Possible<S>) : Action<S> {
}