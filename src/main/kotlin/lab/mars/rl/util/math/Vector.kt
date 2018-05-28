package lab.mars.rl.util.math

import kotlin.math.sqrt
import kotlin.math.PI


data class Vector2(var x: Double = 0.0, var y: Double = 0.0) {
  companion object {
    fun zero() = Vector2(0.0, 0.0)
    val ZERO = zero()
  }
  
  fun set(v: Vector2) {
    x = v.x
    y = v.y
  }
  
  fun set(x: Double, y: Double) {
    this.x = x
    this.y = y
  }
  
  operator fun plus(v: Vector2) = Vector2(x + v.x, y + v.y)
  operator fun plusAssign(v: Vector2) {
    x += v.x
    y += v.y
  }
  
  operator fun minus(v: Vector2) = Vector2(x - v.x, y - v.y)
  operator fun minusAssign(v: Vector2) {
    x -= v.x
    y -= v.y
  }
  
  operator fun times(s: Double) = Vector2(x * s, y * s)
  operator fun timesAssign(s: Double) {
    x *= s
    y *= s
  }
  
  operator fun div(s: Double) = Vector2(x / s, y / s)
  operator fun divAssign(s: Double) {
    x /= s
    y /= s
  }
  
  fun norm(): Vector2 {
    val v = dist()
    x /= v
    y /= v
    return this
  }
  
  fun rot90L(): Vector2 {
    val tmp = x
    x = -y
    y = tmp
    return this
  }
  
  fun rot90R(): Vector2 {
    val tmp = x
    x = y
    y = -tmp
    return this
  }
  
  fun copy() = Vector2(x, y)
  fun dist() = sqrt(x * x + y * y)
  fun dist(v: Vector2) = sqrt((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y))
  
  /** @return the angle in degrees of this vector (point) relative to the x-axis. Angles are towards the positive y-axis
   *         (typically counter-clockwise) and between 0 and 360. */
  fun angle(): Double {
    var angle = Math.atan2(y, x).toFloat() * 180f / PI
    if (angle < 0) angle += 360f
    return angle
  }
  
  fun outOf(_x: Double, _y: Double, width: Double, height: Double): Boolean {
    return x < _x || x > _x + width || y < _y || y > _y + height
  }
  
  fun rotate(degrees: Double): Vector2 {
    val radians = degrees * 180f / PI
    val cos = Math.cos(radians)
    val sin = Math.sin(radians)
    
    val newX = x * cos - y * sin
    val newY = x * sin + y * cos
    
    x = newX
    y = newY
    
    return this
  }
}
