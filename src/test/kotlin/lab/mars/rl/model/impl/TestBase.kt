package lab.mars.rl.model.impl

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.math.argmax
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
val ANSI_BLACK = "\u001B[30m"
val ANSI_RED = "\u001B[31m"
val ANSI_GREEN = "\u001B[32m"
val ANSI_YELLOW = "\u001B[33m"
val ANSI_BLUE = "\u001B[34m"
val ANSI_PURPLE = "\u001B[35m"
val ANSI_CYAN = "\u001B[36m"
val ANSI_WHITE = "\u001B[37m"
val ANSI_RESET = "\u001B[0m"
val ANSI_BLACK_BACKGROUND = "\u001B[40m"
val ANSI_RED_BACKGROUND = "\u001B[41m"
val ANSI_GREEN_BACKGROUND = "\u001B[42m"
val ANSI_YELLOW_BACKGROUND = "\u001B[43m"
val ANSI_BLUE_BACKGROUND = "\u001B[44m"
val ANSI_PURPLE_BACKGROUND = "\u001B[45m"
val ANSI_CYAN_BACKGROUND = "\u001B[46m"
val ANSI_WHITE_BACKGROUND = "\u001B[47m"

val colors = arrayOf(
        ANSI_WHITE_BACKGROUND + ANSI_WHITE,
        ANSI_BLACK_BACKGROUND + ANSI_BLACK,
        ANSI_RED_BACKGROUND + ANSI_RED,
        ANSI_GREEN_BACKGROUND + ANSI_GREEN,
        ANSI_YELLOW_BACKGROUND + ANSI_YELLOW,
        ANSI_BLUE_BACKGROUND + ANSI_BLUE,
        ANSI_PURPLE_BACKGROUND + ANSI_PURPLE,
        ANSI_CYAN_BACKGROUND + ANSI_CYAN)

fun color(idx: Int): String {
    if (idx in 0..colors.lastIndex)
        return colors[idx]
    return idx.toString()
}

fun reset() = ANSI_RESET
fun Double.format(digits: Int) = String.format("%.${digits}f", this)

fun logLevel(level: Level) {
    val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.level = level
}

fun printBlackjack(prob: IndexedMDP, PI: IndexedPolicy, V: StateValueFunction) {
    println("---------------------Usable Ace--------------------------")
    for (a in 9 downTo 0) {
        for (b in 0 until 10) {
            val s = prob.states[1, 1, b, a]
            print("${color(argmax(s.actions) { PI[s, it] }[0])}  ${reset()}")
        }
        println()
    }
    println("---------------------No Usable Ace--------------------------")
    for (a in 9 downTo 0) {
        for (b in 0 until 10) {
            val s = prob.states[1, 0, b, a]
            print("${color(argmax(s.actions) { PI[s, it] }[0])}  ${reset()}")
        }
        println()
    }
    for (a in 0 until 10) {
        for (b in 0 until 10)
            print("${V[1, 1, a, b].format(2)} ")
        println()
    }
    println("------------------------------------------------------------")
    for (a in 0 until 10) {
        for (b in 0 until 10)
            print("${V[1, 0, a, b].format(2)} ")
        println()
    }
}