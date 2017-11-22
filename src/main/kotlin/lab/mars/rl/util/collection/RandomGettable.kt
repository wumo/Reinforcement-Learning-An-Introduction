package lab.mars.rl.util.collection

interface RandomGettable<out E : Any> {
    fun rand(): E
}