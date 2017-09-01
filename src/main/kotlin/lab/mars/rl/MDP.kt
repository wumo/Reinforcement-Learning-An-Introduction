package lab.mars.rl

/**
 * <p>
 * Created on 2017-08-31.
 * </p>
 *
 * @author wumo
 */
typealias StateSet<StateIdx> = IndexedCollection<StateIdx, State<StateIdx>>
typealias StateValueFunction<StateIdx> = IndexedCollection<StateIdx, Double>
typealias ActionValueFunction<StateIdx, ActionIdx> = IndexedCollection<Pair<State<StateIdx>, Action<ActionIdx>>, Double>
typealias DeterminedPolicy<StateIdx, ActionIdx> = IndexedCollection<StateIdx, Action<ActionIdx>>

class MDP<StateIdx, ActionIdx>(
        val states: StateSet<StateIdx>,
        val gamma: Double,
        val v_maker: () -> StateValueFunction<StateIdx>,
        val q_maker: () -> ActionValueFunction<StateIdx, ActionIdx>,
        val pi_maker: () -> DeterminedPolicy<StateIdx, ActionIdx>)

interface IndexedCollection<Idx, E> : Iterable<E> {
    fun get(index: Idx): E
    fun set(index: Idx, s: E)
    fun firstOrNull(): E? {
        val iter = this.iterator()
        return if (iter.hasNext()) iter.next() else null
    }
}

class State<StateIdx> {
    var idx: StateIdx? = null
    var actions: IndexedCollection<Any, Action<Any>>? = null
}

class Action<ActionIdx>(val idx: ActionIdx, val desc: String) {

    var possibles: IndexedCollection<Any, Possible>? = null

    fun sample(): Possible? {
        return null
    }

    override fun toString(): String {
        return desc
    }
}

class Possible {
    var state: State<Any> = empty_state
    var reward: Double = 0.0
    var probability: Double = 0.0

    constructor()

    constructor(state: State<Any>, reward: Double, probability: Double) {
        this.state = state
        this.reward = reward
        this.probability = probability
    }
}

val empty_state = State<Any>()
