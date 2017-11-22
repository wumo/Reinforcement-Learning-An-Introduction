@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.Action
import lab.mars.rl.util.buf.Index
import lab.mars.rl.util.buf.IntBuf
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.math.Rand

class IndexedAction(val index: IntBuf) : Index(), Action<IndexedState> {
    inline override val size: Int
        get() = index.size

    inline override operator fun get(idx: Int) = index[idx]

    var possibles: PossibleSet = emptyNSet as PossibleSet

    override var sample = outer@ {
        if (possibles.isEmpty()) throw NoSuchElementException()
        val p = Rand().nextDouble()
        var acc = 0.0
        for (possible in possibles) {
            acc += possible.probability
            if (p <= acc)
                return@outer possible
        }
        throw IllegalArgumentException("random=$p, but accumulation=$acc")
    }
}