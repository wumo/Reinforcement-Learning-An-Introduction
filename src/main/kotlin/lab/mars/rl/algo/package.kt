package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.collection.*
import lab.mars.rl.util.math.*

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */

typealias EpisodeListener = (Int, Int, State, Double) -> Unit

typealias StepListener = (Int, Int, State, Action<State>) -> Unit

fun V_from_Q(states: StateSet, pvq: OptimalSolution) {
  val (π, V, Q) = pvq
  for (s in states.filter { it.isNotTerminal }) {
    V[s] = Σ(s.actions) {
      π[s, it] * Q[s, it]
    }
  }
}

fun Q_from_V(gamma: Double, states: StateSet, pvq: OptimalSolution) {
  val (_, V, Q) = pvq
  for ((s, a) in states.fork { it.actions })
    Q[s, a] = Σ(a.possibles) { probability * (reward + gamma * V[next]) }
}

fun average_α(indexedMdp: IndexedMDP): (IndexedState, IndexedAction) -> Double {
  val N = indexedMdp.QFunc { 0 }
  return { s, a ->
    N[s, a]++
    1.0 / N[s, a]
  }
}

fun `ε-greedy`(s: IndexedState, Q: ActionValueFunction, π: IndexedPolicy, ε: Double) {
  val a_opt = argmax(s.actions) { Q[s, it] }
  val size = s.actions.size
  for (a in s.actions) {
    π[s, a] = when {
      a === a_opt -> 1 - ε + ε / size
      else -> ε / size
    }
  }
}

fun `ε-greedy`(s: IndexedState, evaluate: Gettable<Action<State>, Double>, π: IndexedPolicy, ε: Double) {
  val a_opt = argmax(s.actions) { evaluate[it] }
  val size = s.actions.size
  for (a in s.actions) {
    π[s, a] = when {
      a === a_opt -> 1 - ε + ε / size
      else -> ε / size
    }
  }
}

fun <E> `ε-greedy`(s: IndexedState, Q: ApproximateFunction<E>, π: IndexedPolicy, ε: Double) {
  val a_opt = argmax(s.actions) { Q(s, it) }
  val size = s.actions.size
  for (a in s.actions) {
    π[s, a] = when {
      a === a_opt -> 1 - ε + ε / size
      else -> ε / size
    }
  }
}

fun `ε-greedy (tie broken randomly)`(s: IndexedState, Q: ActionValueFunction, π: IndexedPolicy, ε: Double) {
  val a_opt = argmax_tie_random(s.actions) { Q[s, it] }
  val size = s.actions.size
  for (a in s.actions) {
    π[s, a] = when {
      a === a_opt -> 1 - ε + ε / size
      else -> ε / size
    }
  }
}