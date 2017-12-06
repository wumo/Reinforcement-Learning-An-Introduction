@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.prediction

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.td.TemporalDifference
import lab.mars.rl.algo.td.`Tabular TD(0)`
import lab.mars.rl.model.ApproximateFunction
import lab.mars.rl.model.impl.func.*
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.model.isTerminal
import lab.mars.rl.problem.`1000-state RandomWalk`
import lab.mars.rl.util.*
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.FastMath.*
import org.junit.Test

class `Tile Coding` {

  @Test
  fun `Tile Coding`() {
    val chart = chart("samples", "state", "value")
    val (prob, PI) = `1000-state RandomWalk`.make()
    val algo = TemporalDifference(prob, PI)
    algo.episodes = 100000
    val V = algo.`Tabular TD(0)`()
    prob.apply {
      val line = line("TD")
      for (s in states) {
        println("${V[s].format(2)} ")
        line[s[0]] = V[s]
      }
      chart += line
    }

    val alpha = 1e-4
    val numOfTilings = 50
    val feature = SimpleTileCoding(numOfTilings,
                                   5,
                                   ceil(`1000-state RandomWalk`.num_states / 5.0).toInt(),
                                   4.0) { (s) -> ((s as IndexedState)[0] - 1).toDouble() }
    val func = LinearFunc(feature)
    val algo2 = FunctionApprox(prob, PI)
    algo2.episodes = 100000
    algo2.α = alpha / numOfTilings
    algo2.`Gradient Monte Carlo algorithm`(func)
    prob.apply {
      val line = line("Tile Coding")
      for (s in states) {
        println("${s[0]}=${func(s).format(2)} ")
        line[s[0]] = func(s)
      }
      chart += line
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }

  @Test
  fun `Tile Coding RMS`() {
    logLevel(Level.ERROR)

    val (prob, PI) = `1000-state RandomWalk`.make()
    val algo = TemporalDifference(prob, PI)
    algo.episodes = 100000
    val V = algo.`Tabular TD(0)`()

    fun RMS(f: ApproximateFunction<Double>): Double {
      var result = 0.0
      for (s in prob.states) {
        if (s.isTerminal) continue
        result += FastMath.pow(V[s] - f(s), 2)
      }
      result /= prob.states.size
      return FastMath.sqrt(result)
    }

    val chart = chart("RMS", "episode", "RMS")
    val episodes = 10000
    val runs = 5
    val alpha = 1e-4
    val numOfTilings = listOf(1, 50)
    runBlocking {
      asyncs(numOfTilings) { numOfTiling ->
        val errors = DoubleArray(episodes)
        asyncs(runs) {
          val func = LinearFunc(
            SimpleTileCoding(
              numOfTiling,
              5,
              ceil(prob.states.size / 5.0).toInt(),
              4.0) { (s) -> ((s as IndexedState)[0] - 1).toDouble() }
          )
          val algo = FunctionApprox(prob, PI)
          algo.α = alpha / numOfTiling
          algo.episodes = episodes
          val _errors = DoubleArray(episodes)
          algo.episodeListener = { episode, _ ->
            _errors[episode - 1] += RMS(func)
          }
          algo.`Gradient Monte Carlo algorithm`(func)
          _errors
        }.await {
          it.forEachIndexed { episode, e ->
            errors[episode] += e
          }
          println("finish Tile coding ($numOfTiling tilings) run: 1")
        }
        val line = line("Tile coding ($numOfTiling tilings) ")
        for (episode in 1..episodes) {
          line[episode] = errors[episode - 1] / runs
        }
        chart += line
        println("finish Tile coding ($numOfTiling tilings)")
      }.await()
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }

  @Test
  fun `Sutton Tile Coding `() {
    val chart = chart("samples", "state", "value")
    val (prob, PI) = `1000-state RandomWalk`.make()
    val algo = TemporalDifference(prob, PI)
    algo.episodes = 100000
    val V = algo.`Tabular TD(0)`()
    prob.apply {
      val line = line("TD")
      for (s in states) {
        println("${V[s].format(2)} ")
        line[s[0]] = V[s]
      }
      chart += line
    }

    val alpha = 1e-4
    val numOfTilings = 32

    val feature = SuttonTileCoding(5, numOfTilings) { (s) ->
      tuple2(doubleArrayOf((s as IndexedState)[0] * 5.0 / `1000-state RandomWalk`.num_states), intArrayOf())
    }

    val func = LinearFunc(feature)
    val algo2 = FunctionApprox(prob, PI)
    algo2.episodes = 100000
    algo2.α = alpha / numOfTilings
    algo2.`Gradient Monte Carlo algorithm`(func)
    prob.apply {
      val line = line("Tile Coding")
      for (s in states) {
        println("${s[0]}=${func(s).format(2)} ")
        line[s[0]] = func(s)
      }
      chart += line
    }
    println("data size=${feature.data.size}")
    feature.data.forEach { k, v -> println("$k=$v") }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }

  @Test
  fun `Sutton Tile Coding RMS`() {
    logLevel(Level.ERROR)

    val (prob, PI) = `1000-state RandomWalk`.make()
    val algo = TemporalDifference(prob, PI)
    algo.episodes = 100000
    val V = algo.`Tabular TD(0)`()

    fun <E> RMS(f: ApproximateFunction<E>): Double {
      var result = 0.0
      for (s in prob.states) {
        if (s.isTerminal) continue
        result += pow(V[s] - f(s), 2)
      }
      result /= prob.states.size
      return sqrt(result)
    }

    val chart = chart("RMS", "episode", "RMS")

    val episodes = 10000
    val runs = 5
    val alpha = 1e-4
    val numOfTilings = listOf(4, 32)
    runBlocking {
      val numOfTiling = 1
      val errors = DoubleArray(episodes) { 0.0 }
      asyncs(runs) {
        val algo = FunctionApprox(prob, PI)
        algo.episodes = episodes
        val _errors = DoubleArray(episodes) { 0.0 }
        val func = LinearFunc(
          SimpleTileCoding(numOfTiling,
                           5,
                           ceil(prob.states.size / 5.0).toInt(),
                           4.0) { (s) -> ((s as IndexedState)[0] - 1).toDouble() }
        )
        algo.α = alpha / numOfTiling
        algo.episodeListener = { episode, _ ->
          _errors[episode - 1] += RMS(func)
        }
        algo.`Gradient Monte Carlo algorithm`(func)
        _errors
      }.await {
        it.forEachIndexed { episode, e ->
          errors[episode] += e
        }
        println("finish Tile coding ($numOfTiling tilings) run: 1")
      }

      val line = line("Tile coding ($numOfTiling tilings) ")
      for (episode in 1..episodes) {
        line[episode] = errors[episode - 1] / runs
      }
      chart += line
      println("finish Tile coding ($numOfTiling tilings)")
    }

    runBlocking {
      asyncs(numOfTilings) { numOfTiling ->
        val errors = DoubleArray(episodes) { 0.0 }
        asyncs(runs) {
          val algo = FunctionApprox(prob, PI)
          algo.episodes = episodes
          val _errors = DoubleArray(episodes) { 0.0 }
          val func = LinearFunc(
            SuttonTileCoding(5,
                             numOfTiling) { (s) -> tuple2(doubleArrayOf((s as IndexedState)[0] * 5.0 / `1000-state RandomWalk`.num_states), intArrayOf()) }
          )
          algo.α = alpha / numOfTiling
          algo.episodeListener = { episode, _ ->
            _errors[episode - 1] += RMS(func)
          }
          algo.`Gradient Monte Carlo algorithm`(func)
          _errors
        }.await {
          it.forEachIndexed { episode, e ->
            errors[episode] += e
          }
          println("finish Tile coding ($numOfTiling tilings) run: 1")
        }

        val line = line("Tile coding ($numOfTiling tilings) ")
        for (episode in 1..episodes)
          line[episode] = errors[episode - 1] / runs
        chart += line
        println("finish Tile coding ($numOfTiling tilings)")
      }.await()
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}