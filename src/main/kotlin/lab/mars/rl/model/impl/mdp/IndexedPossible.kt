package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.Possible

class IndexedPossible(next: IndexedState, reward: Double, var probability: Double) : Possible<IndexedState>(next, reward) {
  override operator fun component1() = next
  override operator fun component2() = reward
  operator fun component3() = probability
}