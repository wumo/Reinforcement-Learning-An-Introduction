package lab.mars.rl.util.collection

interface Gettable<in K : Any, out V : Any> {
    operator fun get(k: K): V
}