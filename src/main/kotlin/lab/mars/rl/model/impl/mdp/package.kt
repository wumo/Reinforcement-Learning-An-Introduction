package lab.mars.rl.model.impl.mdp

import lab.mars.rl.util.collection.IndexedCollection
import lab.mars.rl.util.tuples.tuple3

typealias StateSet = IndexedCollection<IndexedState>
typealias PossibleSet = IndexedCollection<IndexedPossible>
typealias StateValueFunction = IndexedCollection<Double>
typealias ActionValueFunction = IndexedCollection<Double>
typealias OptimalSolution = tuple3<IndexedPolicy, StateValueFunction, ActionValueFunction>
