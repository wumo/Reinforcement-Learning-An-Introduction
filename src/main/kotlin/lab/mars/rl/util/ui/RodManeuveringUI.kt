package lab.mars.rl.util.ui

import javafx.application.Application
import javafx.application.Platform.runLater
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.model.impl.mdp.StateValueFunction
import lab.mars.rl.problem.RodManeuvering
import lab.mars.rl.problem.RodManeuvering.currentStatus
import lab.mars.rl.problem.RodManeuvering.height
import lab.mars.rl.problem.RodManeuvering.resolution
import lab.mars.rl.problem.RodManeuvering.rodEdges
import lab.mars.rl.problem.RodManeuvering.rotate
import lab.mars.rl.problem.RodManeuvering.rotation_resolution
import lab.mars.rl.problem.RodManeuvering.unit_x
import lab.mars.rl.problem.RodManeuvering.unit_y
import lab.mars.rl.problem.RodManeuvering.width
import lab.mars.rl.util.math.max
import java.util.concurrent.CyclicBarrier

class RodManeuveringUI: Application() {
  lateinit var canvas: Canvas
  
  companion object {
    var after: () -> Unit = {}
    var render: (StateValueFunction, IndexedState) -> Unit = { _, _ -> }
  }
  
  override fun start(ps: Stage?) {
    val primaryStage = ps!!
//    primaryStage.title = "Drawing Operations Test"
    val root = Group()
    canvas = Canvas(width, height)
    drawMap()
    root.children.add(canvas)
    primaryStage.scene = Scene(root)
    primaryStage.show()
    render = this::render
    after()
  }
  
  fun drawMap() {
    val gc = canvas.graphicsContext2D
    gc.stroke = Color.BLACK
    for (o in RodManeuvering.obstacles) {
      o.v.apply {
        val xPoints = DoubleArray(size) { this[it].x }
        val yPoints = DoubleArray(size) { this[it].y }
        gc.strokePolygon(xPoints,
                         yPoints, size)
      }
    }
  }
  
  val barrier = CyclicBarrier(2)
  var max = 1.0
  var min = 0.0
  fun render(V: StateValueFunction, s: IndexedState) {
    barrier.reset()
    runLater {
      val (x, y, rotation) = currentStatus(s)
      val gc = canvas.graphicsContext2D
      gc.clearRect(0.0, 0.0, width, height)
      gc.stroke = Color.BLACK
      for (nx in 0 until resolution)
        for (ny in 0 until resolution) {
          val value = max(0 until rotation_resolution) { V[nx, ny, it] }
          max = maxOf(max, value)
          min = minOf(min, value)
          gc.fill = Color.BLUE.interpolate(Color.RED, if (max == min) 0.5 else (value - min) / (max - min))
          gc.fillRect(nx * unit_x, ny * unit_y, unit_x, unit_y)
        }
      gc.fill = Color.GREEN
      for (edge in rodEdges) {
        val p1 = edge._1.rotate(rotation).add(x, y)
        val p2 = edge._2.rotate(rotation).add(x, y)
        gc.strokeLine(p1.x, p1.y, p2.x, p2.y)
      }
      
      drawMap()
      barrier.await()
    }
    barrier.await()
  }
  
}