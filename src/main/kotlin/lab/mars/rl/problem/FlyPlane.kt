package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.DefaultMDP
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.dimension.cnsetFrom
import lab.mars.rl.util.math.*
import lab.mars.rl.util.math.Vector2.Companion.ZERO

object FlyPlane {
  val width = 600.0
  private val numSimulation = 10
  private val Δt = 0.1
  private val max_acc = 1.0
  val oLoc = Vector2(300.0, 300.0)
  val oRadius = 100.0
  private val targetLoc = Vector2(550.0, 50.0)
  private val targetRadius = 50.0
  private val initloc = Vector2(0.0, 600.0)
  val planeRadius = 10.0
  private val initVel = Vector2(7.0, -7.0)
  private val maxFuel = 10000.0
  
  class PlaneState(
      val loc: Vector2,
      val vel: Vector2,
      val oLoc: Array<Vector2>,
      val oRadius: Array<Double>,
      val targetLoc: Vector2,
      val targetRadius: Double,
      val fuel: Double,
      val terminal: Boolean
  ) : State {
    override val actions: RandomIterable<Action<PlaneState>> =
        if (terminal) emptyNSet()
        else cnsetFrom(3) {
          val a = it[0]
          DefaultAction(a) {
            val newLoc = loc.copy()
            val newVel = vel.copy()
            repeat(numSimulation) {
              val dir = newVel.copy().norm()
              val acc = when (a) {
                1 -> //left
                  dir.rot90L() * max_acc
                2 -> //right
                  dir.rot90R() * max_acc
                else ->//no op
                  ZERO
              }
              newLoc += newVel * Δt + acc * (Δt * Δt * 0.5)
              newVel += acc * Δt
            }
            val newFuel = fuel - 1.0
            var terminal = false
            var reward = loc.dist(targetLoc) - newLoc.dist(targetLoc)
            when {
              newLoc.outOf(0.0, 0.0, width, width) ||//out of field
              newFuel < 0 -> {//out of fuel
                terminal = true
                reward = -1000.0
              }
              hitObstacles(newLoc, oLoc, oRadius) -> {//hit obstacle
                terminal = true
                reward = -2000.0
              }
              newLoc.dist(targetLoc) <= targetRadius -> {//hit destination
                terminal = true
                reward = 1000.0 - (maxFuel - newFuel)
              }
            }
            Possible(PlaneState(newLoc, newVel, oLoc, oRadius, targetLoc, targetRadius, newFuel, terminal), reward)
          }
        }
    val isAtTarget: Boolean
      get() = loc.dist(targetLoc) <= targetRadius
  }
  
  fun hitObstacles(loc: Vector2, oLoc: Array<Vector2>, oRadius: Array<Double>): Boolean {
    val size = oLoc.size
    for (i in 0 until size) {
      if (loc.dist(oLoc[i]) <= oRadius[i])
        return true
    }
    return false
  }
  
  fun make() = DefaultMDP(1.0) {
    PlaneState(loc = initloc,
               vel = initVel,
               oLoc = arrayOf(oLoc),
               oRadius = arrayOf(oRadius),
               targetLoc = targetLoc,
               targetRadius = targetRadius,
               fuel = maxFuel,
               terminal = false)
  }
  
  fun make(numObstacles: Int = 1, maxObstacleRadius: Double = oRadius): MDP {
    val oLoc = Array(numObstacles) {
      Vector2(Rand().nextDouble(maxObstacleRadius, width - targetRadius * 2 - maxObstacleRadius),
              Rand().nextDouble(maxObstacleRadius, width - targetRadius * 2 - maxObstacleRadius))
    }
    val oRadius = Array(numObstacles) {
      maxObstacleRadius * Rand().nextDouble()
    }
    return DefaultMDP(1.0) {
      PlaneState(loc = initloc,
                 vel = initVel,
                 oLoc = oLoc,
                 oRadius = oRadius,
                 targetLoc = targetLoc,
                 targetRadius = targetRadius,
                 fuel = maxFuel,
                 terminal = false)
    }
  }
  
  fun makeRand(numObstacles: Int = 1, minObstacleRadius: Double = oRadius - 1.0, maxObstacleRadius: Double = oRadius): MDP {
    val numObstacles = minOf(numObstacles, 16)
    val iter = IntArray(16) {
      it
    }.toMutableList().shuffled().iterator()
    val oLoc = Array(numObstacles) {
      val idx = iter.next()
      val x = idx / 4
      val y = idx % 4
      Vector2(150.0 + 100.0 * x, 150.0 + 100.0 * y)
    }
    val oRadius = Array(numObstacles) {
      Rand().nextDouble(minObstacleRadius, maxObstacleRadius)
    }
    return DefaultMDP(0.9) {
      PlaneState(loc = initloc,
                 vel = initVel,
                 oLoc = oLoc,
                 oRadius = oRadius,
                 targetLoc = targetLoc,
                 targetRadius = targetRadius,
                 fuel = maxFuel,
                 terminal = false)
    }
  }
  
  fun makeRand2(numObstacles: Int = 1, maxObstacleRadius: Double = oRadius): MDP {
    return DefaultMDP(1.0) {
      val oLoc = Array(numObstacles) {
        Vector2(Rand().nextDouble(maxObstacleRadius, width - targetRadius * 2 - maxObstacleRadius),
                Rand().nextDouble(maxObstacleRadius, width - targetRadius * 2 - maxObstacleRadius))
      }
      val oRadius = Array(numObstacles) {
        maxObstacleRadius * Rand().nextDouble()
      }
      PlaneState(loc = initloc,
                 vel = initVel,
                 oLoc = oLoc,
                 oRadius = oRadius,
                 targetLoc = targetLoc,
                 targetRadius = targetRadius,
                 fuel = maxFuel,
                 terminal = false)
    }
  }
}