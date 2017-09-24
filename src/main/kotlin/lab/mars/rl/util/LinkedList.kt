package lab.mars.rl.util

import com.sun.istack.internal.localization.NullLocalizable


/**
 * Constructs an empty list.
 */
val null_obj = object : Any() {}

inline fun <T> Null(): T = null_obj as T
class LinkedList<E> : Iterable<E> {
    @Transient internal var size = 0

    /**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     * (first.prev == null && first.item != null)
     */
    @Transient internal var first: Node<E> = Null()

    /**
     * Pointer to last node.
     * Invariant: (first == null && last == null) ||
     * (last.next == null && last.item != null)
     */
    @Transient internal var last: Node<E> = Null()

    fun first(): E {
        return first.item
    }

    /**
     * Links e as last element.
     */
    fun addLast(e: E) {
        val L = last
        val newNode = Node(L, e, Null())
        last = newNode
        if (L === null_obj)
            first = newNode
        else
            L.next = newNode
        size++
    }

    fun addLast(another: LinkedList<E>): LinkedList<E> {
        if (another.isEmpty()) return this
        val L = last
        val newNode = another.first
        newNode.prev = L
        last = another.last
        if (L === null_obj)
            first = newNode
        else
            L.next = newNode
        size += another.size
        return this
    }

    operator fun plus(another: LinkedList<E>): LinkedList<E> {
        val result = LinkedList<E>()
        result.addLast(this).addLast(another)
        return result
    }

    fun subList(start: Int): LinkedList<E> {
        var a = 0
        var startNode = first
        while (a < start)
            startNode = startNode.next
        val result = LinkedList<E>()
        result.first = startNode
        result.last = last
    }

    /**
     * Unlinks non-null node x.
     */
    fun unlink(x: Node<E>): E {
        // assert x != null;
        val element = x.item
        val next = x.next
        val prev = x.prev

        if (prev == null) {
            first = next
        } else {
            prev.next = next
            x.prev = null
        }

        if (next == null) {
            last = prev
        } else {
            next.prev = prev
            x.next = null
        }

        x.item = null
        size--
        return element
    }

    fun size(): Int {
        return size
    }

    fun isEmpty() = size == 0

    override fun iterator(): Iterator<E> {
        return object : Iterator<E> {
            internal var x = first

            override fun hasNext(): Boolean {
                return x !== null_obj
            }

            override fun next(): E {
                val tmp = x.item
                x = x.next
                return tmp
            }
        }
    }

    class Node<E> internal constructor(var prev: Node<E>, var item: E, var next: Node<E>) {

        fun item(): E {
            return item
        }

        operator fun next(): Node<E>? {
            return next
        }

        fun prev(): Node<E>? {
            return prev
        }
    }
}
