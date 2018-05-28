@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.problem

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import lab.mars.rl.algo.EpisodeListener
import lab.mars.rl.algo.StepListener
import lab.mars.rl.algo.eligibility_trace.control.`True Online Sarsa(λ)`
import lab.mars.rl.algo.func_approx.play
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.DefaultAction
import lab.mars.rl.model.impl.mdp.EpsilonGreedyFunctionPolicy
import lab.mars.rl.problem.RodManeuvering.resolution
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.D2DGameUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.math.ceil
import kotlin.math.log
import kotlin.math.sqrt

class `Test FlyPlane Problem with TD λ` {
  @Test
  fun `Fly Plane UI`() {
    val numTilings = 10
    val feature = SuttonTileCoding(numTilesPerTiling = 2000, _numTilings = numTilings, allowCollisions = true) { (s, a) ->
      s as FlyPlane.PlaneState
      a as DefaultAction<Int, FlyPlane.PlaneState>
      val floats = ArrayList<Double>(FlyPlane.numObstaclesPerStage + 4).apply {
        add(s.loc.x / 100.0);add(s.loc.y / 100.0);add(s.vel.x / 10.0);add(s.vel.y / 10.0)
        for (i in 0 until FlyPlane.numObstaclesPerStage) {
          val obstacle = FlyPlane.stageObstacles[s.stage][i]
          add(obstacle.loc.x / 100.0);add(obstacle.loc.y / 100.0);add(obstacle.radius / 100.0)
        }
      }.toDoubleArray()
      tuple2(floats, intArrayOf(a.value))
    }
    val func = LinearFunc(feature)
    
    FlyPlane.maxStage = 16
    FlyPlane.numObstaclesPerStage = 5
    val resolution = 100
    val unit = FlyPlane.fieldWidth / resolution
    val m = ceil(sqrt(FlyPlane.maxStage.toDouble())).toInt()
    val stageWidth = FlyPlane.fieldWidth / m
    val stageScale = 1.0 / m
    fun Double.transX(stage: Int) = stageWidth * (stage / m) + this * stageScale
    fun Double.transY(stage: Int) = stageWidth * (stage % m) + this * stageScale
    fun Double.tranUnit() = this * stageScale
    
    val qvalue = Array(FlyPlane.maxStage) { Array(resolution + 1) { Array(resolution + 1) { Double.NEGATIVE_INFINITY } } }
    var accuG = 0.0
    val maxG = DoubleArray(FlyPlane.maxStage + 1) { Double.NEGATIVE_INFINITY }
    var wins = 0.0
    var win_step = 0.0
    val episode_round = 100
    val step_round = 10
    val max_episode = 10000
    var episode_base = 0
    var animate = false
    val latch = CountDownLatch(1)
    thread {
      latch.await()
      
      val episodeListener: EpisodeListener = { episode, step, st, G ->
        println("${feature.data.size}->${feature.numOfComponents}")
        accuG += G
        maxG[FlyPlane.maxStage] = maxOf(maxG[FlyPlane.maxStage], G)
        st as FlyPlane.PlaneState
        if (st.isAtIntial) {
          wins++
          win_step += step
        }
        if (episode % episode_round == 0) {
          val averageG = accuG / episode_round
          val win_rate = wins / episode_round
          val average_win_step = if (wins <= 0) 0.0 else win_step / wins
          accuG = 0.0
          wins = 0.0
          win_step = 0.0
          Platform.runLater {
            D2DGameUI.charts[0].data.apply {
              for (i in 0 until FlyPlane.maxStage)
                if (!maxG[i].isInfinite())
                  this[i].add(XYChart.Data(episode + episode_base, maxG[i]))
              this[FlyPlane.maxStage].add(XYChart.Data(episode + episode_base, maxG[FlyPlane.maxStage] / FlyPlane.maxStage))
            }
            D2DGameUI.charts[1].data[0].add(XYChart.Data(episode + episode_base, win_rate))
            if (average_win_step > 0)
              D2DGameUI.charts[2].data[0].add(XYChart.Data(episode + episode_base, average_win_step))
            D2DGameUI.charts[3].data[0].add(XYChart.Data(episode + episode_base, averageG))
          }
        }
      }
      val stepListener: StepListener = step@{ episode, step, s, a, Gt ->
        s as FlyPlane.PlaneState
        a as DefaultAction<Int, FlyPlane.PlaneState>
        
        val nx = Math.floor(s.loc.x / unit).toInt()
        val ny = Math.floor(s.loc.y / unit).toInt()
        qvalue[s.stage][nx][ny] = maxOf(qvalue[s.stage][nx][ny], func(s, a))
        if (s.isAtIntial)
          maxG[s.stage] = maxOf(maxG[s.stage], Gt)
        if (episode % step_round != 0) return@step
        if (!animate && step > 1) return@step
        D2DGameUI.render { gc ->
          gc.clearRect(0.0, 0.0, FlyPlane.fieldWidth, FlyPlane.fieldWidth)
          var max = Double.NEGATIVE_INFINITY
          var min = Double.POSITIVE_INFINITY
          for (stage in 0 until FlyPlane.maxStage)
            for (nx in 0 until resolution)
              for (ny in 0 until resolution) {
                val q = qvalue[stage][nx][ny]
                if (q.isInfinite()) continue
                max = maxOf(max, q)
                min = minOf(min, q)
              }
          gc.lineWidth = 3.0
          for (stage in 0 until FlyPlane.maxStage) {
            for (nx in 0 until resolution)
              for (ny in 0 until resolution) {
                var q = qvalue[stage][nx][ny]
                if (q.isInfinite())
                  q = min
                gc.fill = Color.BLUE.interpolate(Color.RED, if (max == min) 0.5 else (q - min) / (max - min))
                gc.fillRect((nx * unit).transX(stage), (ny * unit).transY(stage), unit.tranUnit(), unit.tranUnit())
              }
            gc.stroke = Color.BLACK
            gc.strokeRect(0.0.transX(stage), 0.0.transY(stage), FlyPlane.fieldWidth.tranUnit(), FlyPlane.fieldWidth.tranUnit())
          }
          D2DGameUI.title = "max=$max,min=$min"
          with(s) {
            for (stage in 0 until FlyPlane.maxStage) {
              gc.stroke = Color.GREEN
              gc.strokeOval((FlyPlane.target.loc.x - FlyPlane.target.radius).transX(stage),
                            (FlyPlane.target.loc.y - FlyPlane.target.radius).transY(stage),
                            2 * FlyPlane.target.radius.tranUnit(),
                            2 * FlyPlane.target.radius.tranUnit())
              gc.stroke = Color.RED
              for (i in 0 until FlyPlane.numObstaclesPerStage) {
                val obstacle = FlyPlane.stageObstacles[stage][i]
                val oLoc = obstacle.loc
                val oRadius = obstacle.radius
                gc.strokeOval((oLoc.x - oRadius).transX(stage), (oLoc.y - oRadius).transY(stage),
                              2 * oRadius.tranUnit(), 2 * oRadius.tranUnit())
              }
            }
            gc.fill = Color.BLACK
            val dir = s.vel.copy().norm() * FlyPlane.plane.radius
            val top = s.loc + dir
            val left = s.loc + (dir / 2.0).rot90L()
            val right = s.loc + (dir / 2.0).rot90R()
            gc.fillPolygon(doubleArrayOf(top.x.transX(stage), left.x.transX(stage), right.x.transX(stage)),
                           doubleArrayOf(top.y.transY(stage), left.y.transY(stage), right.y.transY(stage)),
                           3)
          }
        }
      }
      val prob = FlyPlane.makeRand(minObstacleRadius = 50.0, maxObstacleRadius = 100.0, γ = 0.9)
      animate = false
      prob.`True Online Sarsa(λ)`(
          Qfunc = func,
          π = EpsilonGreedyFunctionPolicy(func, 0.1),
          λ = 0.96,
          α = 0.6 / numTilings,
          episodes = max_episode,
//          z_maker = { m, n -> SparseMatrix(m, n) },
          episodeListener = episodeListener,
          stepListener = stepListener
      )
      animate = true
      prob.play(
          π = EpsilonGreedyFunctionPolicy(func, 0.0),
          episodes = 10,
          stepListener = { _, _, s, a ->
            stepListener(0, 0, s, a, 0.0)
            Thread.sleep(Math.floor(1000 / 60.0).toLong())
          }
      )
      for (stage in 0 until FlyPlane.maxStage)
        for (nx in 0..qvalue.lastIndex)
          for (ny in 0..qvalue[nx].lastIndex)
            qvalue[stage][nx][ny] = Double.NEGATIVE_INFINITY
      episode_base += max_episode
    }
    D2DGameUI.apply {
      canvas_width = FlyPlane.fieldWidth
      canvas_height = FlyPlane.fieldWidth
      width = 1200.0
      height = 1000.0
      charts.addAll(D2DGameUI.ChartDescription("max return per $episode_round episodes", "episode", "max return",
                                               numSeries = FlyPlane.maxStage + 1, yForceZeroInRange = false),
                    D2DGameUI.ChartDescription("win rate per $episode_round episodes", "episode", "win rate"),
                    D2DGameUI.ChartDescription("average win step per $episode_round episodes", "episode", "average win step",
                                               yForceZeroInRange = false),
                    D2DGameUI.ChartDescription("average return per $episode_round episodes", "episode", "average return"))
      afterStartup = { gc ->
        latch.countDown()
      }
    }
    Application.launch(D2DGameUI::class.java)
  }
}