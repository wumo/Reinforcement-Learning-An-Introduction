package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.collection.Gettable
import lab.mars.rl.util.math.*

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */

fun V_from_Q(states: StateSet, pvq: OptimalSolution) {
  val (π, V, Q) = pvq
  for (s in states)
    s.actions.ifAny {
      V[s] = Σ(this) {
        π[s, it] * Q[s, it]
      }
    }
}

fun Q_from_V(gamma: Double, states: StateSet, pvq: OptimalSolution) {
  val (_, V, Q) = pvq
  for ((s, a) in states { actions })
    Q[s, a] = Σ(a.possibles) { probability * (reward + gamma * V[next]) }
}

fun average_alpha(indexedMdp: IndexedMDP): (IndexedState, IndexedAction) -> Double {
  val N = indexedMdp.QFunc { 0 }
  return { s, a ->
    N[s, a]++
    1.0 / N[s, a]
  }
}

fun `ε-greedy`(s: IndexedState, Q: ActionValueFunction, policy: IndexedPolicy, ε: Double) {
  val a_opt = argmax(s.actions) { Q[s, it] }
  val size = s.actions.size
  for (a in s.actions) {
    policy[s, a] = when {
      a === a_opt -> 1 - ε + ε / size
      else -> ε / size
    }
  }
}

fun `ε-greedy`(s: IndexedState, evaluate: Gettable<Action<State>, Double>, q: IndexedPolicy, ε: Double) {
  val a_opt = argmax(s.actions) { evaluate[it] }
  val size = s.actions.size
  for (a in s.actions) {
    q[s, a] = when {
      a === a_opt -> 1 - ε + ε / size
      else -> ε / size
    }
  }
}

fun <E> `ε-greedy`(s: IndexedState, Q: ApproximateFunction<E>, policy: IndexedPolicy, ε: Double) {
  val a_opt = argmax(s.actions) { Q(s, it) }
  val size = s.actions.size
  for (a in s.actions) {
    policy[s, a] = when {
      a === a_opt -> 1 - ε + ε / size
      else -> ε / size
    }
  }
}

fun `ε-greedy (tie broken randomly)`(s: IndexedState, Q: ActionValueFunction, policy: IndexedPolicy, ε: Double) {
  val a_opt = argmax_tie_random(s.actions) { Q[s, it] }
  val size = s.actions.size
  for (a in s.actions) {
    policy[s, a] = when {
      a === a_opt -> 1 - ε + ε / size
      else -> ε / size
    }
  }
}