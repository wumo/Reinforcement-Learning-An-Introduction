@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.DefaultMDP
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.dimension.cnsetFrom
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.math.Vector2
import lab.mars.rl.util.math.Vector2.Companion.ZERO
import java.util.*

object FlyPlane {
  data class RigidBody(val loc: Vector2, val radius: Double)
  
  val fieldWidth = 600.0
  private val numSimulation = 1
  private val Δt = 1.0
  private val max_acc = 1.0
  val target = RigidBody(Vector2(550.0, 50.0), 50.0)
  val plane = RigidBody(Vector2(0.0, 600.0), 10.0)
  private val initVel = Vector2(7.0, -7.0)
  private val maxFuel = 10000.0
  var maxStage: Int = 5
  var numObstaclesPerStage = 1
  lateinit var stageObstacles: Array<Array<RigidBody>>
  
  class PlaneState(
      val loc: Vector2,
      val vel: Vector2,
      val fuel: Double,
      val terminal: Boolean,
      val stage: Int,
      val remainStages: LinkedList<Int>
  ) : State {
    override val actions: RandomIterable<Action<PlaneState>> =
        if (terminal) emptyNSet()
        else cnsetFrom(3) {
          val a = it[0]
          DefaultAction(a) {
            val newLoc = loc.copy()
            val newVel = vel.copy()
            var newStage = stage
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
            var newFuel = fuel - 1.0
            var terminal = false
            val obstacles = stageObstacles[stage]
            var reward = loc.dist(target.loc) - newLoc.dist(target.loc)
            when {
              newLoc.outOf(0.0, 0.0, fieldWidth, fieldWidth) ||//out of field
              newFuel < 0 -> {//out of fuel
                terminal = true
                reward = -1000.0
              }
              collide(newLoc, obstacles) -> {//hit obstacle
                terminal = true
                reward = -2000.0
              }
              collide(newLoc, target) -> {//hit destination
                reward = 1000.0 - (maxFuel - newFuel)
                newLoc.set(plane.loc)
                newFuel = maxFuel
                newVel.set(initVel)
                if (remainStages.isEmpty())
                  terminal = true
                else
                  newStage = remainStages.pop()
              }
            }
            Possible(PlaneState(newLoc, newVel, newFuel, terminal, newStage, remainStages), reward)
          }
        }
    val isAtTarget: Boolean
      get() = collide(loc, target)
    val isAtIntial: Boolean
      get() = collide(loc, plane)
  }
  
  inline fun collide(loc: Vector2, obstacle: RigidBody): Boolean {
    if (loc.dist(obstacle.loc) <= obstacle.radius)
      return true
    return false
  }
  
  fun collide(loc: Vector2, obstacles: Array<RigidBody>): Boolean {
    val size = obstacles.size
    for (i in 0 until size)
      if (collide(loc, obstacles[i]))
        return true
    return false
  }
  
  fun makeRand(minObstacleRadius: Double = 50.0,
               maxObstacleRadius: Double = 100.0,
               γ: Double = 1.0): MDP {
    FlyPlane.numObstaclesPerStage = minOf(numObstaclesPerStage, 16)
    stageObstacles = Array(maxStage) {
      val iter = (0 until 16).shuffled().iterator()
      Array(numObstaclesPerStage) {
        val idx = iter.next()
        val x = idx / 4
        val y = idx % 4
        RigidBody(Vector2(150.0 + 100.0 * x, 150.0 + 100.0 * y), Rand().nextDouble(minObstacleRadius, maxObstacleRadius))
      }
    }
    val tmp = MutableList(maxStage) { it }
    val remainStages = LinkedList<Int>()
    return DefaultMDP(γ) {
      tmp.shuffle()
      remainStages.clear()
      remainStages.addAll(tmp)
      
      PlaneState(loc = plane.loc,
                 vel = initVel,
                 fuel = maxFuel,
                 terminal = false,
                 stage = remainStages.removeFirst(),
                 remainStages = remainStages)
    }
  }
}