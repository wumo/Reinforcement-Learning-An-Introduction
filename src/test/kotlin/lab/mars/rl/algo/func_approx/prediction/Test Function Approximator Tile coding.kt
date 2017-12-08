@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.prediction

import ch.qos.logback.classic.Level
import javafx.application.Application
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.algo.td.`Tabular TD(0)`
import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.*
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.problem.`1000-state RandomWalk`
import lab.mars.rl.util.*
import lab.mars.rl.util.collection.filter
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.*
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.FastMath.*
import org.junit.Test

class `Tile Coding` {
  
  @Test
  fun `Tile Coding`() {
    val chart = LineChart("samples", "state", "value")
    val (prob, π) = `1000-state RandomWalk`.make()
    val V = prob.`Tabular TD(0)`(π = π, episodes = 100000, α = 0.1)
    prob.apply {
      val line = Line("TD")
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
    prob.`Gradient Monte Carlo algorithm`(func, π = π, α = alpha / numOfTilings, episodes = 100000)
    prob.apply {
      val line = Line("Tile Coding")
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
    
    val (prob, π) = `1000-state RandomWalk`.make()
    val V = prob.`Tabular TD(0)`(π = π, episodes = 100000, α = 0.1)
    
    fun RMS(f: ApproximateFunction<Double>): Double {
      var result = 0.0
      for (s in prob.states.filter { it.isNotTerminal })
        result += FastMath.pow(V[s] - f(s), 2)
      result /= prob.states.size
      return FastMath.sqrt(result)
    }
    
    val chart = LineChart("RMS", "episode", "RMS")
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
          val _errors = DoubleArray(episodes)
          prob.`Gradient Monte Carlo algorithm`(
              v = func, π = π,
              α = alpha / numOfTiling,
              episodes = episodes,
              episodeListener = { episode, _ ->
                _errors[episode - 1] += RMS(func)
              })
          _errors
        }.await {
          it.forEachIndexed { episode, e ->
            errors[episode] += e
          }
          println("finish Tile coding ($numOfTiling tilings) run: 1")
        }
        val line = Line("Tile coding ($numOfTiling tilings) ")
        for (episode in 1..episodes)
          line[episode] = errors[episode - 1] / runs
        chart += line
        println("finish Tile coding ($numOfTiling tilings)")
      }.await()
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
  
  @Test
  fun `Sutton Tile Coding `() {
    val chart = LineChart("samples", "state", "value")
    val (prob, π) = `1000-state RandomWalk`.make()
    val V = prob.`Tabular TD(0)`(π = π, episodes = 100000, α = 0.1)
    prob.apply {
      val line = Line("TD")
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
    prob.`Gradient Monte Carlo algorithm`(v = func, π = π, α = alpha / numOfTilings, episodes = 100000)
    prob.apply {
      val line = Line("Tile Coding")
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
    
    val (prob, π) = `1000-state RandomWalk`.make()
    val V = prob.`Tabular TD(0)`(π = π, episodes = 100000, α = 0.1)
    
    fun <E> RMS(f: ApproximateFunction<E>): Double {
      var result = 0.0
      for (s in prob.states.filter { it.isNotTerminal })
        result += pow(V[s] - f(s), 2)
      result /= prob.states.size
      return sqrt(result)
    }
    
    val chart = LineChart("RMS", "episode", "RMS")
    
    val episodes = 10000
    val runs = 5
    val alpha = 1e-4
    val numOfTilings = listOf(4, 32)
    runBlocking {
      val numOfTiling = 1
      val errors = DoubleArray(episodes) { 0.0 }
      asyncs(runs) {
        val _errors = DoubleArray(episodes) { 0.0 }
        val func = LinearFunc(
            SimpleTileCoding(numOfTiling,
                             5,
                             ceil(prob.states.size / 5.0).toInt(),
                             4.0) { (s) -> ((s as IndexedState)[0] - 1).toDouble() }
        )
        prob.`Gradient Monte Carlo algorithm`(
            v = func, π = π,
            α = alpha / numOfTiling,
            episodes = episodes,
            episodeListener = { episode, _ ->
              _errors[episode - 1] += RMS(func)
            })
        _errors
      }.await {
        it.forEachIndexed { episode, e ->
          errors[episode] += e
        }
        println("finish Tile coding ($numOfTiling tilings) run: 1")
      }
      
      val line = Line("Tile coding ($numOfTiling tilings) ")
      for (episode in 1..episodes)
        line[episode] = errors[episode - 1] / runs
      chart += line
      println("finish Tile coding ($numOfTiling tilings)")
    }
    
    runBlocking {
      asyncs(numOfTilings) { numOfTiling ->
        val errors = DoubleArray(episodes) { 0.0 }
        asyncs(runs) {
          val _errors = DoubleArray(episodes) { 0.0 }
          val func = LinearFunc(
              SuttonTileCoding(5,
                               numOfTiling) { (s) ->
                tuple2(doubleArrayOf((s as IndexedState)[0] * 5.0 / `1000-state RandomWalk`.num_states), intArrayOf())
              })
          prob.`Gradient Monte Carlo algorithm`(
              v = func, π = π,
              α = alpha / numOfTiling,
              episodes = episodes,
              episodeListener = { episode, _ ->
                _errors[episode - 1] += RMS(func)
              })
          _errors
        }.await {
          it.forEachIndexed { episode, e ->
            errors[episode] += e
          }
          println("finish Tile coding ($numOfTiling tilings) run: 1")
        }
        val line = Line("Tile coding ($numOfTiling tilings) ")
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