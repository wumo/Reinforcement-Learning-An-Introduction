package lab.mars.rl.util

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlin.math.sign



inline fun <R> listOf(size: Int, init: (Int) -> R): ArrayList<R> {
  val list = ArrayList<R>()
  for (i in 0 until size)
    list += init(i)
  return list
}

inline fun <I, R> listOf(iter: Iterable<I>, init: (I) -> R): ArrayList<R> {
  val list = ArrayList<R>()
  for (i in iter)
    list += init(i)
  return list
}

inline fun <I1, I2, R> listOf(iter1: Iterable<I1>, iter2: Iterable<I2>, init: (I1, I2) -> R): List<R> {
  val list = mutableListOf<R>()
  for (i in iter1)
    for (j in iter2)
      list += init(i, j)
  return list
}

fun <R> asyncs(size: Int, init: suspend (Int) -> R): ArrayList<Deferred<R>> {
  val list = ArrayList<Deferred<R>>()
  for (i in 0 until size)
    list += async {
      init(i)
    }
  
  return list
}

fun <I, R> asyncs(iter: Iterable<I>, init: suspend (I) -> R): ArrayList<Deferred<R>> {
  val list = ArrayList<Deferred<R>>()
  for (i in iter)
    list += async {
      init(i)
    }
  
  return list
}

fun <I1, I2, R> asyncs(iter1: Iterable<I1>, iter2: Iterable<I2>, init: suspend (I1, I2) -> R): ArrayList<Deferred<R>> {
  val list = ArrayList<Deferred<R>>()
  for (i in iter1)
    for (j in iter2)
      list += async { init(i, j) }
  return list
}

suspend fun <R> ArrayList<Deferred<R>>.await(process: suspend (R) -> Unit = {}) {
  forEach { process(it.await()) }
}