package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.*
import lab.mars.rl.util.math.*
import kotlin.math.exp

class SoftmaxpPolicy<E>(val π: ApproximateFunction<E>) : Policy {
  override fun invoke(s: State): Action<State> {
    return rand(s.actions) { exp(π(s, it)) }
  }
  
  override fun get(s: State, a: Action<State>) = exp(π(s, a))
  
  override fun greedy(s: State) = rand(s.actions) { exp(π(s, it)) }
}