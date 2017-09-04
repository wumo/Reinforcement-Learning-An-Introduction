package lab.mars.rl

/**
 * <p>
 * Created on 2017-08-31.
 * </p>
 *
 * @author wumo
 */
typealias StateSet = IndexedCollection<State?>
typealias StateValueFunction = IndexedCollection<Double>
typealias ActionValueFunction = IndexedCollection<Double>
typealias DeterminedPolicy = IndexedCollection<Action?>

class MDP(
        val states: StateSet,
        val gamma: Double,
        val v_maker: () -> StateValueFunction,
        val q_maker: () -> ActionValueFunction,
        val pi_maker: () -> DeterminedPolicy)

interface Indexable {
    val idx: IntArray
}

interface IndexedCollection<E> : Iterable<E> {
    operator fun get(vararg index: Int): E
    operator fun get(indexable: Indexable): E
    operator fun get(vararg indexable: Indexable): E
    operator fun set(vararg index: Int, s: E)
    operator fun set(indexable: Indexable, s: E)
    operator fun set(vararg indexable: Indexable, s: E)
}

class State(vararg index: Int) : Indexable {
    override val idx = index

    var actions: IndexedCollection<Action>? = null

    override fun toString() = idx.asList().toString()
}

class Action(vararg index: Int) : Indexable {
    override val idx = index

    var possibles: IndexedCollection<Possible>? = null

    fun sample(): Possible? = null

    override fun toString() = idx.asList().toString()
}

class Possible {
    var state: State? = null
    var reward: Double = 0.0
    var probability: Double = 0.0

    constructor()

    constructor(state: State, reward: Double, probability: Double) {
        this.state = state
        this.reward = reward
        this.probability = probability
    }
}
