package lab.mars.rl.util.ui

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab.mars.rl.model.impl.mdp.*
import java.util.concurrent.CyclicBarrier

class GridWorldUI : Application() {
  lateinit var canvas: Canvas

  companion object {
    var after: () -> Unit = {}
    var render: (ActionValueFunction, IndexedState) -> Unit = { _, _ -> }
    var width = 450.0
    var height = 300.0
    var grid_x = 9
    var grid_y = 6
  }

  override fun start(ps: Stage?) {
    val primaryStage = ps!!
    primaryStage.title = "Drawing Operations Test"
    val root = Group()
    canvas = Canvas(width, height)
    root.children.add(canvas)
    primaryStage.scene = Scene(root)
    primaryStage.show()
    render = this::render
    after()
  }

  val barrier = CyclicBarrier(2)
  var max = 1.0
  var min = 0.0
  fun render(V: StateValueFunction, s: IndexedState) {
    barrier.reset()
    Platform.runLater {
      val gc = canvas.graphicsContext2D
      gc.clearRect(0.0, 0.0, width, height)
      gc.stroke = Color.BLACK
      val u_x = width / grid_x
      val u_y = height / grid_y
      for ((dim, value) in V.withIndices()) {
        max = maxOf(max, value)
        min = minOf(min, value)
        val nx = dim[0]
        val ny = dim[1]
        gc.fill = Color.BLUE.interpolate(Color.RED, if (max == min) 0.5 else (value - min) / (max - min))
        val x = u_x * nx
        val y = u_y * ny
        gc.fillRect(x, y, u_x, u_y)
      }
      gc.fill = Color.GREEN
      gc.fillRect(s[0] * u_x, s[1] * u_y, u_x, u_y)
      for ((dim, value) in V.withIndices()) {
        max = maxOf(max, value)
        val nx = dim[0]
        val ny = dim[1]
        val x = u_x * nx
        val y = u_y * ny
        gc.strokeRect(x, y, u_x, u_y)
      }
      barrier.await()
    }
    barrier.await()
  }

}