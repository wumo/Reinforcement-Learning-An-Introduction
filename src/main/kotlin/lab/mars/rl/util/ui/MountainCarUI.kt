package lab.mars.rl.util.ui

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.stage.Stage
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.problem.CarState
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.util.ui.GridWorldUI.Companion.grid_x
import lab.mars.rl.util.ui.GridWorldUI.Companion.grid_y
import java.util.concurrent.CyclicBarrier
import kotlin.math.PI
import kotlin.math.sin

class MountainCarUI: Application() {
  lateinit var canvas: Canvas
  
  companion object {
    var render: (Int, Int, CarState, DefaultAction<Int, CarState>) -> Unit = { _, _, _, _ -> }
    var after: () -> Unit = {}
    var width = 450.0
    var height = 300.0
  }
  
  override fun start(ps: Stage?) {
    val primaryStage = ps!!
    primaryStage.title = "Mountain Car"
    val root = Group()
    canvas = Canvas(width, height)
    root.children.add(canvas)
    primaryStage.scene = Scene(root)
    primaryStage.show()
    render = this::render
    after()
  }
  
  val barrier = CyclicBarrier(2)
  fun tx(x: Double) = (x + PI / 2) / (2 * PI / 3) * width
  fun ty(y: Double) = (-y + 1) / 2 * height
  fun render(episode: Int, step: Int, s: CarState, a: DefaultAction<Int, CarState>) {
    barrier.reset()
    Platform.runLater {
      val gc = canvas.graphicsContext2D
      gc.clearRect(0.0, 0.0, width, height)
      gc.stroke = Color.BLACK
      gc.strokeText("episode:$episode\nstep:$step", width / 2-50, height / 2)
      for (i in 0..40) {
        val x1 = i / 40.0 * 2 * PI / 3
        val y1 = sin(3 * (x1 + PI / 6))
        val x2 = (i + 1) / 40.0 * 2 * PI / 3
        val y2 = sin(3 * (x2 + PI / 6))
        gc.strokeLine(i / 40.0 * width, ty(y1), (i + 1) / 40.0 * width, ty(y2))
      }
      val min_x=tx(MountainCar.POSITION_MIN)
      val min_y=ty(sin(3*MountainCar.POSITION_MIN))
      gc.strokeLine(min_x,min_y,min_x+10,min_y)
      val ball_x = tx(s.position)
      val ball_y = ty(sin(3 * s.position))
      gc.strokeOval(ball_x, ball_y, 10.0, 10.0)
      gc.stroke = Color.RED
      gc.strokeLine(ball_x, ball_y, ball_x + a.value * 40, ball_y)
      barrier.await()
    }
    Thread.sleep(30)
    barrier.await()
  }
}