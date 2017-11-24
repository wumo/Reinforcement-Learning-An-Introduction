@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util.ui

import javafx.animation.*
import javafx.application.Application
import javafx.geometry.Point3D
import javafx.scene.*
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.*
import javafx.scene.transform.Rotate
import javafx.scene.transform.Translate
import javafx.stage.Stage
import javafx.util.Duration
import org.apache.commons.math3.util.FastMath.max
import org.apache.commons.math3.util.FastMath.min
import tornadofx.plusAssign

class RawD3DChartUI : Application() {
    class RawD3DChart(val gridx: Int, val gridy: Int,
                   val width: Double = 10.0,
                   val height: Double = 10.0,
                   val depth: Double = 10.0,
                   value: (Int, Int) -> Double) {
        var min_y = Float.POSITIVE_INFINITY
        var max_y = Float.NEGATIVE_INFINITY
        val vertexSize = 3
        val faceSize = 2 * 3 * 2
        val surface: MeshView

        init {
            val x_unit = width / gridx
            val y_unit = height / gridy

            val mesh = TriangleMesh().apply {
                texCoords.addAll(0f, 0f)
                val vertices = FloatArray(vertexSize * gridx * gridy)
                val faces = IntArray((gridx - 1) * (gridy - 1) * faceSize)
                for (x in 0 until gridx)
                    for (y in 0 until gridy) {
                        val v = value(x, y).toFloat()
                        min_y = min(min_y, v)
                        max_y = max(max_y, v)
                        val idx = v_idx(x, y) * vertexSize
                        vertices[idx] = (x * x_unit).toFloat()
                        vertices[idx + 1] = (y * y_unit).toFloat()
                        vertices[idx + 2] = v
                    }
                for (x in 0 until gridx)
                    for (y in 0 until gridy) {
                        val idx = v_idx(x, y) * vertexSize + 2
                        vertices[idx] = normalize(vertices[idx])
                    }
                for (x in 0 until gridx - 1)
                    for (y in 0 until gridy - 1) {
                        val A = v_idx(x, y)
                        val B = v_idx(x + 1, y)
                        val C = v_idx(x + 1, y + 1)
                        val D = v_idx(x, y + 1)
                        val f = f_idx(x, y)
                        faces[f] = A;faces[f + 2] = B;faces[f + 4] = C
                        faces[f + 6] = C;faces[f + 8] = D;faces[f + 10] = A
                    }
            }
            surface = MeshView(mesh)
        }

        inline fun v_idx(x: Int, y: Int) = x * gridx + y
        inline fun f_idx(x: Int, y: Int) = (x * ((gridx - 1)) + y) * faceSize

        private fun normalize(y: Float)
                = if (max_y == min_y) 0f else (depth * (y - min_y) / (max_y - min_y)).toFloat()
    }

    companion object {
        private val charts = mutableListOf<RawD3DChart>()
    }

    override fun start(primaryStage: Stage?) {
        val flowPane = FlowPane()
        for (chart in charts)
            flowPane += createContent(chart.surface)
        val scene = Scene(flowPane, 800.0, 800.0)
        primaryStage!!.scene = scene
        primaryStage.show()
    }

    private fun createContent(surface: MeshView): SubScene {
        val x = line(Point3D(0.0, 0.0, 0.0), Point3D(100.0, 0.0, 0.0))
        x.material = PhongMaterial(Color.RED)
        val y = line(Point3D(0.0, 0.0, 0.0), Point3D(0.0, 100.0, 0.0))
        y.material = PhongMaterial(Color.GREEN)
        val z = line(Point3D(0.0, 0.0, 0.0), Point3D(0.0, 0.0, 100.0))
        z.material = PhongMaterial(Color.BLUE)

        val pivot = Translate()
        val yRotate = Rotate(0.0, Rotate.Y_AXIS)

        // Create and position camera
        val camera = PerspectiveCamera(true)
        camera.transforms.addAll(
                pivot,
                yRotate,
                Rotate(-45.0, Rotate.X_AXIS),
                Translate(0.0, 0.0, -50.0)
        )

        // animate the camera position.
        val timeline = Timeline(
                KeyFrame(
                        Duration.seconds(0.0),
                        KeyValue(yRotate.angleProperty(), 0)
                ),
                KeyFrame(
                        Duration.seconds(15.0),
                        KeyValue(yRotate.angleProperty(), 360)
                )
        )
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()

        // Build the Scene Graph
        val root = Group(camera, x, y, z, surface)

        // Use a SubScene
        val subScene = SubScene(
                root,
                300.0, 300.0,
                true,
                SceneAntialiasing.BALANCED
        )
        subScene.fill = Color.ALICEBLUE
        subScene.camera = camera

        return subScene
    }

    fun line(origin: Point3D, target: Point3D): Cylinder {
        val yAxis = Point3D(0.0, 1.0, 0.0)
        val diff = target.subtract(origin)
        val height = diff.magnitude()

        val mid = target.midpoint(origin)
        val moveToMidpoint = Translate(mid.getX(), mid.getY(), mid.getZ())

        val axisOfRotation = diff.crossProduct(yAxis)
        val angle = Math.acos(diff.normalize().dotProduct(yAxis))
        val rotateAroundCenter = Rotate(-Math.toDegrees(angle), axisOfRotation)

        val line = Cylinder(0.1, height)

        line.transforms.addAll(moveToMidpoint, rotateAroundCenter)

        return line
    }
}