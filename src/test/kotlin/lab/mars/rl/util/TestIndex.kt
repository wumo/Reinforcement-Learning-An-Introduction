@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.util

import lab.mars.rl.util.buf.*
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * <p>
 * Created on 2017-09-18.
 * </p>
 *
 * @author wumo
 */
class TestIndex {
  @Test
  fun `range forEach`() {
    val indices = arrayOf(DefaultIntBuf.of(0),
                          DefaultIntBuf.of(1, 2, 3),
                          DefaultIntBuf.of(4, 5, 6, 7))
    val _idx = MultiIndex(indices as Array<Index>)
    val expected = IntArray(8) { it }
    _idx.forEach(0, 0) { idx, value ->
      assertEquals(expected[idx], value)
    }
    _idx.forEach(4, 7) { idx, value ->
      assertEquals(expected[idx], value)
    }
    _idx.forEach { idx, value ->
      assertEquals(expected[idx], value)
    }
    _idx.forEach(2, 5) { idx, value ->
      assertEquals(expected[idx], value)
    }
    _idx.forEach(0, 5) { idx, value ->
      assertEquals(expected[idx], value)
    }
  }
}