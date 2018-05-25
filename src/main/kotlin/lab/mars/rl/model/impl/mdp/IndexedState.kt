@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.State
import lab.mars.rl.util.buf.Index
import lab.mars.rl.util.buf.IntBuf
import lab.mars.rl.util.collection.IndexedCollection
import lab.mars.rl.util.collection.emptyNSet

class IndexedState(val index: IntBuf): Index(), State {
  override inline val size: Int
    get() = index.size
  
  override inline operator fun get(idx: Int) = index[idx]
  
  override var actions: IndexedCollection<IndexedAction> = emptyNSet()
}