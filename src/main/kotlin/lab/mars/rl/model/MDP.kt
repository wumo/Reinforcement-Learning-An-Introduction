package lab.mars.rl.model

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

    fun init(maker: (IntArray) -> E)
}

class A {
    operator fun get(vararg index: Int): Int {
        return 0
    }
}

class State(vararg index: Int) : Indexable {
    override val idx = index

    var actions: IndexedCollection<Action> = emptyActions

    override fun toString() = idx.asList().toString()
}

class Action(vararg index: Int) : Indexable {
    override val idx = index

    var possibles: IndexedCollection<Possible> = emptyPossibles

    fun sample(): Possible? = null

    override fun toString() = idx.asList().toString()
}

class Possible(var next: State, var reward: Double, var probability: Double)

val emptyActions = object : IndexedCollection<Action> {
    override fun get(vararg index: Int): Action {
        TODO("not implemented")
    }

    override fun get(indexable: Indexable): Action {
        TODO("not implemented")
    }

    override fun get(vararg indexable: Indexable): Action {
        TODO("not implemented")
    }

    override fun set(vararg index: Int, s: Action) {
        TODO("not implemented")
    }

    override fun set(indexable: Indexable, s: Action) {
        TODO("not implemented")
    }

    override fun set(vararg indexable: Indexable, s: Action) {
        TODO("not implemented")
    }

    override fun iterator(): Iterator<Action> {
        return emptyActionIterator
    }

    override fun init(maker: (IntArray) -> Action) {
        TODO("not implemented")
    }
}

val emptyActionIterator = object : Iterator<Action> {
    override fun hasNext() = false

    override fun next() = null as Action
}

val emptyPossibles = object : IndexedCollection<Possible> {
    override fun get(vararg index: Int): Possible {
        TODO("not implemented")
    }

    override fun get(indexable: Indexable): Possible {
        TODO("not implemented")
    }

    override fun get(vararg indexable: Indexable): Possible {
        TODO("not implemented")
    }

    override fun set(vararg index: Int, s: Possible) {
        TODO("not implemented")
    }

    override fun set(indexable: Indexable, s: Possible) {
        TODO("not implemented")
    }

    override fun set(vararg indexable: Indexable, s: Possible) {
        TODO("not implemented")
    }

    override fun init(maker: (IntArray) -> Possible) {
        TODO("not implemented")
    }

    override fun iterator(): Iterator<Possible> {
        return emptyPossibleIterator
    }

}

val emptyPossibleIterator = object : Iterator<Possible> {
    override fun hasNext() = false

    override fun next() = null as Possible
}
