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
  val height = 600.0
  private val numSimulation = 1
  private val Δt = 1.0
  private val max_acc = 1.0
  private val oLoc = Vector2(300.0, 300.0)
  private val oRadius = 50.0
  private val targetLoc = Vector2(500.0, 100.0)
  private val targetRadius = 50.0
  private val initloc = Vector2(100.0, 500.0)
  val planeRadius = 10.0
  private val initVel = Vector2(7.0, -7.0)
  
  class PlaneState(
      val loc: Vector2,
      val vel: Vector2,
      val oLoc: Vector2,
      val oRadius: Double,
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
            val loc = loc.copy()
            val vel = vel.copy()
            val oLoc = oLoc.copy()
            val targetLoc = targetLoc.copy()
            repeat(numSimulation) {
              val dir = vel.copy().norm()
              val acc = when (a) {
                1 -> //left
                  dir.rot90L() * max_acc
                2 -> //right
                  dir.rot90R() * max_acc
                else ->//no op
                  ZERO
              }
              loc += vel * Δt + acc * (Δt * Δt * 0.5)
              vel += acc * Δt
            }
            val fuel = fuel - 1.0
            var terminal = false
            var reward = -1.0
            when {
              loc.outOf(0.0, 0.0, width, height) ||//out of field
              loc.dist(oLoc) <= oRadius ||//hit obstacle
              fuel < 0 -> {//out of fuel
                terminal = true
                reward = -1000.0
              }
              loc.dist(targetLoc) <= targetRadius -> {//hit destination
                terminal = true
                reward = 1000.0
              }
            }
            Possible(PlaneState(loc, vel, oLoc, oRadius, targetLoc, targetRadius, fuel, terminal), reward)
          }
        }
    val isAtTarget: Boolean
      get() = loc.dist(targetLoc) <= targetRadius
  }
  
  fun make() = DefaultMDP(1.0) {
    PlaneState(loc = initloc,
               vel = initVel,
               oLoc = oLoc,
               oRadius = oRadius,
               targetLoc = targetLoc,
               targetRadius = targetRadius,
               fuel = 10000.0,
               terminal = false)
  }
}