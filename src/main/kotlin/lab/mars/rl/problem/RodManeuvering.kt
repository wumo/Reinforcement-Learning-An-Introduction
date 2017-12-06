package lab.mars.rl.problem

import javafx.geometry.Point2D
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isTerminal
import lab.mars.rl.util.buf.DefaultBuf
import lab.mars.rl.util.collection.cnsetOf
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.dimension.x
import lab.mars.rl.util.tuples.tuple2
import org.apache.commons.math3.util.FastMath.*

object RodManeuvering {
  class Obstacle(vararg val v: Point2D) {
    fun contains(x: Double, y: Double): Boolean {
      for (n in 0..v.lastIndex) {
        val a = v[n]
        val b = v[(n + 1) % v.size]
        val p = Point2D(x, y)
        val affine_segment = b.subtract(a)
        val affine_point = p.subtract(a)
        if (affine_segment.crossProduct(affine_point).z < 0)
          return false
      }
      return true
    }
    
    fun intersect(p1: Point2D, p2: Point2D): Boolean {
      for (n in 0..v.lastIndex) {
        val a = v[n]
        val b = v[(n + 1) % v.size]
        if (intersect(p1, p2, a, b))
          return true
      }
      return false
    }
    
    /**
     * implementation of http://www.dcs.gla.ac.uk/~pat/52233/slides/Geometry1x1.pdf
     */
    private fun intersect(p1: Point2D, p2: Point2D, p3: Point2D, p4: Point2D): Boolean {
      val o1 = orientation(p1, p2, p3)
      val o2 = orientation(p1, p2, p4)
      val o3 = orientation(p3, p4, p1)
      val o4 = orientation(p3, p4, p2)
      if (o1 == 0.0 && o1 == o2 && o2 == o3 && o3 == o4) {//colinear
        val r1 = range(p1.x, p2.x)
        val r2 = range(p3.x, p4.x)
        val r3 = range(p1.y, p2.y)
        val r4 = range(p3.y, p4.y)
        return intersect(r1, r2) && intersect(r3, r4)
      }
      return o1 != o2 && o3 != o4
    }
    
    fun orientation(v1: Point2D, v2: Point2D, v3: Point2D): Double {
      val ori = (v2.y - v1.y) * (v3.x - v2.x) - (v3.y - v2.y) * (v2.x - v1.x)
      return signum(ori)
    }
    
    fun range(a: Double, b: Double) = if (a < b) a..b else b..a
    fun intersect(a: ClosedRange<Double>, b: ClosedRange<Double>)
        = a.start in b || a.endInclusive in b || b.start in a || b.endInclusive in a
  }
  
  val rodLength = 100.0
  val rodWidth = 5.0
  
  val rodEdges = DefaultBuf.new<tuple2<Point2D, Point2D>>(4)
  
  val obstacles = DefaultBuf.new<Obstacle>(8)
  
  val width = 400.0
  val height = 400.0
  val resolution = 20
  val rotation_resolution = 18
  val unit_x = width / resolution
  val unit_y = height / resolution
  val unit_rotation = Math.PI / rotation_resolution
  
  init {
    rodEdges.apply {
      append(tuple2(Point2D(-rodWidth / 2, -rodLength / 2), Point2D(rodWidth / 2, -rodLength / 2)))
      append(tuple2(Point2D(rodWidth / 2, -rodLength / 2), Point2D(rodWidth / 2, rodLength / 2)))
      append(tuple2(Point2D(rodWidth / 2, rodLength / 2), Point2D(-rodWidth / 2, rodLength / 2)))
      append(tuple2(Point2D(-rodWidth / 2, rodLength / 2), Point2D(-rodWidth / 2, -rodLength / 2)))
    }
    
    obstacles.apply {
      append(Obstacle(Point2D(82.0, 273.0), Point2D(86.0, 187.0), Point2D(157.0, 238.0), Point2D(153.0, 326.0)))
      append(Obstacle(Point2D(87.0, 156.0), Point2D(36.0, 122.0), Point2D(97.0, 135.0), Point2D(150.0, 170.0)))
      append(Obstacle(Point2D(78.0, 104.0), Point2D(64.0, 57.0), Point2D(99.0, 23.0), Point2D(113.0, 69.0)))
      append(Obstacle(Point2D(191.0, 68.0), Point2D(235.0, 76.0), Point2D(251.0, 117.0), Point2D(206.0, 109.0)))
      append(Obstacle(Point2D(340.0, 53.0), Point2D(321.0, 10.0), Point2D(364.0, 30.0), Point2D(386.0, 74.0)))
      append(Obstacle(Point2D(287.0, 97.0), Point2D(311.0, 179.0), Point2D(297.0, 262.0), Point2D(273.0, 182.0)))
      append(Obstacle(Point2D(275.0, 323.0), Point2D(271.0, 286.0), Point2D(300.0, 311.0), Point2D(305.0, 347.0)))
      append(Obstacle(Point2D(295.0, 360.0), Point2D(334.0, 335.0), Point2D(379.0, 345.0), Point2D(340.0, 371.0)))
      //edge
      append(Obstacle(Point2D(-10.0, -10.0), Point2D(0.0, -10.0), Point2D(0.0, 410.0), Point2D(-10.0, 410.0)))
      append(Obstacle(Point2D(-10.0, -10.0), Point2D(410.0, -10.0), Point2D(410.0, 0.0), Point2D(-10.0, 0.0)))
      append(Obstacle(Point2D(400.0, -10.0), Point2D(410.0, -10.0), Point2D(410.0, 410.0), Point2D(400.0, 410.0)))
      append(Obstacle(Point2D(-10.0, 400.0), Point2D(410.0, 400.0), Point2D(410.0, 410.0), Point2D(-10.0, 410.0)))
    }
  }
  
  fun intersect(x: Double, y: Double, rotation: Double): Boolean {
    for (obstacle in obstacles) {
      if (obstacle.contains(x, y))
        return true
      for (edge in rodEdges) {
        val p1 = edge._1.rotate(rotation).add(x, y)
        val p2 = edge._2.rotate(rotation).add(x, y)
        if (obstacle.intersect(p1, p2))
          return true
      }
    }
    return false
  }
  
  fun Point2D.rotate(angleRad: Double)
      = Point2D(x * cos(angleRad) - y * sin(angleRad), x * sin(angleRad) + y * cos(angleRad))
  
  fun make(): IndexedMDP {
    val mdp = mdpOf(gamma = 0.95, state_dim = resolution x resolution x rotation_resolution, action_dim = 6)
    return mdp.apply {
      val goal = states[17, 6, 0]
      goal.actions = emptyNSet()
      for (s in states) {
        val (x, y, rotation) = currentStatus(s)
        if (intersect(x, y, rotation)) {
          s.actions = emptyNSet()
          continue
        }
      }
      for (s in states) {
        if (s.isTerminal) continue
        val (x, y, rotation) = currentStatus(s)
        s.actions.apply {
          fun IndexedAction.assign(nx: Int, ny: Int, r: Int) {
            var s_next: IndexedState
            if (nx in 0 until resolution
                && ny in 0 until resolution) {
              s_next = states[nx, ny, (r + rotation_resolution) % rotation_resolution]
              if (s_next.isTerminal && s_next !== goal)
                s_next = s
            } else
              s_next = s
            possibles = cnsetOf(
                if (s_next === goal) IndexedPossible(s_next, 1.0, 1.0)
                else IndexedPossible(s_next, 0.0, 1.0))
          }
          
          this[0].assign(s[0], s[1], s[2] - 1)//turn clockwise
          this[1].assign(s[0], s[1], s[2] + 1)//turn counter-clockwise
          
          var nextPos = Point2D(x, y).add(Point2D(0.0, unit_y).rotate(rotation))
          var nx = floor(nextPos.x / unit_x).toInt()
          var ny = floor(nextPos.y / unit_y).toInt()
          this[2].assign(nx, ny, s[2])//move forward along the long axis
          
          nextPos = Point2D(x, y).add(Point2D(0.0, -unit_y).rotate(rotation))
          nx = floor(nextPos.x / unit_x).toInt()
          ny = floor(nextPos.y / unit_y).toInt()
          this[3].assign(nx, ny, s[2])//move backward along the long axis
          
          nextPos = Point2D(x, y).add(Point2D(unit_x, 0.0).rotate(rotation))
          nx = floor(nextPos.x / unit_x).toInt()
          ny = floor(nextPos.y / unit_y).toInt()
          this[4].assign(nx, ny, s[2])//move forward perpendicular to the long axis
          
          nextPos = Point2D(x, y).add(Point2D(-unit_x, 0.0).rotate(rotation))
          nx = floor(nextPos.x / unit_x).toInt()
          ny = floor(nextPos.y / unit_y).toInt()
          this[5].assign(nx, ny, s[2])//move backward perpendicular to the long axis
        }
      }
      started = { states(3, 13, 0).rand() }
    }
  }
  
  fun currentStatus(s: IndexedState): Triple<Double, Double, Double> {
    val x = unit_x * (s[0] + 0.5)
    val y = unit_y * (s[1] + 0.5)
    val rotation = unit_rotation * s[2]
    return Triple(x, y, rotation)
  }
}