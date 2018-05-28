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
  private var γ: Double = 1.0
  
  class PlaneState(
      val loc: Vector2,
      val vel: Vector2,
      val fuel: Double,
      val stage: Int,
      val remainStages: LinkedList<Int>,
      val stageEnd: Boolean,
      val G: Double
  ) : State {
    override val actions: RandomIterable<Action<PlaneState>> =
        if (stage == -1) emptyNSet()
        else cnsetFrom(3) {
          val a = it[0]
          DefaultAction(a) {
            val nextLoc = loc.copy()
            val nextVel = vel.copy()
            var nextFuel: Double
            var nextStage = stage
            var nextStageEnd = false
            var G = G
            var reward: Double
            if (collide(loc, target)) {
              nextLoc.set(plane.loc)
              nextVel.set(initVel)
              nextFuel = maxFuel
              nextStage = if (remainStages.isEmpty()) -1 else remainStages.pop()
              nextStageEnd = false
              G = 0.0
              reward = 0.0
            } else {
              repeat(numSimulation) {
                val dir = nextVel.copy().norm()
                val acc = when (a) {
                  1 -> //left
                    dir.rot90L() * max_acc
                  2 -> //right
                    dir.rot90R() * max_acc
                  else ->//no op
                    ZERO
                }
                nextLoc += nextVel * Δt + acc * (Δt * Δt * 0.5)
                nextVel += acc * Δt
              }
              nextFuel = fuel - 1.0
              val obstacles = stageObstacles[stage]
              reward = loc.dist(target.loc) - nextLoc.dist(target.loc)
              when {
                nextLoc.outOf(0.0, 0.0, fieldWidth, fieldWidth) ||//out of field
                nextFuel < 0 -> {//out of fuel
                  reward = -1000.0
                  nextStage = -1
                  nextStageEnd = true
                }
                collide(nextLoc, obstacles) -> {//hit obstacle
                  reward = -2000.0
                  nextStage = -1
                  nextStageEnd = true
                }
                collide(nextLoc, target) -> {//hit destination
                  reward = 1000.0 - (maxFuel - nextFuel)
                  nextLoc.set(target.loc)
                  nextFuel = maxFuel
                  nextVel.set(initVel)
                  nextStageEnd = true
                  nextStage = stage
                }
              }
              G += γ * reward
            }
            Possible(PlaneState(nextLoc, nextVel, nextFuel, nextStage, remainStages, nextStageEnd, G), reward)
          }
        }
    val isAtTarget
      get() = collide(loc, target)
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
    FlyPlane.γ = γ
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
                 stage = remainStages.removeFirst(),
                 remainStages = remainStages,
                 stageEnd = false,
                 G = 0.0)
    }
  }
}