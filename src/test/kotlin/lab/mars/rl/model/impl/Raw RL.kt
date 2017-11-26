package lab.mars.rl.model.impl

import javafx.application.Application
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.math.argmax_tie_random
import lab.mars.rl.util.tuples.tuple3
import lab.mars.rl.util.ui.*
import org.apache.commons.math3.util.FastMath.*

//all possible actions
val ACTION_REVERSE = -1
val ACTION_ZERO = 0
val ACTION_FORWARD = 1
// order is important
val ACTIONS = listOf(ACTION_REVERSE, ACTION_ZERO, ACTION_FORWARD)

// bound for position and velocity
val POSITION_MIN = -1.2
val POSITION_MAX = 0.5
val VELOCITY_MIN = -0.07
val VELOCITY_MAX = 0.07

// use optimistic initial value, so it's ok to set epsilon to 0
val EPSILON = 0

/* take an @action at @position and @velocity
* @return: new position, new velocity, reward (always -1)
*/
fun takeAction(position: Double, velocity: Double, action: Int): tuple3<Double, Double, Double> {
    var newVelocity = velocity + 0.001 * action - 0.0025 * cos(3 * position)
    newVelocity = min(max(VELOCITY_MIN, newVelocity), VELOCITY_MAX)
    var newPosition = position + newVelocity
    newPosition = min(max(POSITION_MIN, newPosition), POSITION_MAX)
    val reward = -1.0
    if (newPosition == POSITION_MIN)
        newVelocity = 0.0
    return tuple3(newPosition, newVelocity, reward)
}

// wrapper class for state action value function
class ValueFunction(stepSize: Double, val numOfTilings: Int = 8, maxSize: Int = 2048) {
    /* In this example I use the tiling software instead of implementing standard tiling by myself
     * One important thing is that tiling is only a map from (state, action) to a series of indices
     * It doesn't matter whether the indices have meaning, only if this map satisfy some property
     * View the following webpage for more information
     * http://incompleteideas.net/sutton/tiles/tiles3.html
     * @maxSize: the maximum # of indices
     */

    // divide step size equally to each tiling
    val stepSize = stepSize / numOfTilings

    val hashTable = IHT(maxSize)

    // weight for each tile
    val weights = DoubleArray(maxSize)

    // position and velocity needs scaling to satisfy the tile software
    val positionScale = numOfTilings / (POSITION_MAX - POSITION_MIN)
    val velocityScale = numOfTilings / (VELOCITY_MAX - VELOCITY_MIN)

    // get indices of active tiles for given state and action
    fun getActiveTiles(position: Double, velocity: Double, action: Int): List<Int> {
        /* I think positionScale * (position - position_min) would be a good normalization .
        * However positionScale * position_min is a constant, so it's ok to ignore it.
        */
        val activeTiles = hashTable.tiles(numOfTilings,
                                          listOf(positionScale * position, velocityScale * velocity),
                                          listOf(action))
        return activeTiles
    }

    // estimate the value of given state and action
    fun value(position: Double, velocity: Double, action: Int): Double {
        if (position == POSITION_MAX)
            return 0.0
        val activeTiles = getActiveTiles(position, velocity, action)
        return weights[activeTiles].sum()
    }

    // learn with given state, action and target
    fun learn(position: Double, velocity: Double, action: Int, target: Double) {
        val activeTiles = getActiveTiles(position, velocity, action)
        val estimation = weights[activeTiles].sum()
        val delta = stepSize * (target - estimation)
        for (activeTile in activeTiles)
            weights[activeTile] += delta
    }

    // get # of steps to reach the goal under current state value function
    fun costToGo(position: Double, velocity: Double): Double {
        val costs = mutableListOf<Double>()
        for (action in ACTIONS)
            costs.add(value(position, velocity, action))
        return -costs.max()!!
    }
}

private operator fun DoubleArray.get(activeTiles: List<Int>) =
        DoubleArray(activeTiles.size) {
            this[activeTiles[it]]
        }

// get action at @position and @velocity based on epsilon greedy policy and @valueFunction
fun getAction(position: Double, velocity: Double, valueFunction: ValueFunction): Int {
    if (Rand().nextDouble() < EPSILON)
        return random_choice(ACTIONS)
    val values = mutableListOf<Double>()
    for (action in ACTIONS)
        values.add(valueFunction.value(position, velocity, action))
    return argmax_tie_random(ACTIONS) { values[it + 1] }
}

fun <E> random_choice(actions: List<E>) = actions[Rand().nextInt(actions.size)]

/* semi-gradient n-step Sarsa
* @valueFunction: state value function to learn
* @n: # of steps
*/
fun semiGradientNStepSarsa(valueFunction: ValueFunction, n: Int = 1): Int {
    // start at a random position around the bottom of the valley
    var currentPosition = Rand().nextDouble(-0.6, -0.4)
    // initial velocity is 0
    var currentVelocity = 0.0
    // get initial action
    var currentAction = getAction(currentPosition, currentVelocity, valueFunction)

    // track previous position, velocity, action and reward
    val positions = mutableListOf(currentPosition)
    val velocities = mutableListOf(currentVelocity)
    val actions = mutableListOf(currentAction)
    val rewards = mutableListOf(0.0)

    // track the time
    var time = 0

    // the length of this episode
    var T = Int.MAX_VALUE
    while (true) {
        // go to next time step
        time += 1

        if (time < T) {
            // take current action and go to the new state
            val (newPostion, newVelocity, reward) = takeAction(currentPosition, currentVelocity, currentAction)
            // choose new action
            val newAction = getAction(newPostion, newVelocity, valueFunction)

            currentPosition = newPostion
            currentVelocity = newVelocity
            currentAction = newAction

            // track new state and action
            positions.add(newPostion)
            velocities.add(newVelocity)
            actions.add(newAction)
            rewards.add(reward)

            if (newPostion == POSITION_MAX)
                T = time
        }
        // get the time of the state to update
        val updateTime = time - n
        if (updateTime >= 0) {
            var returns = 0.0
            // calculate corresponding rewards
            for (t in (updateTime + 1..min(T, updateTime + n)))
                returns += rewards[t]
            //add estimated state action value to the return
            if (updateTime + n <= T)
                returns += valueFunction.value(positions[updateTime + n],
                                               velocities[updateTime + n],
                                               actions[updateTime + n])
            // update the state value function
            if (positions[updateTime] != POSITION_MAX)
                valueFunction.learn(positions[updateTime], velocities[updateTime], actions[updateTime], returns)
        }
        if (updateTime == T - 1)
            break
    }
    return time
}

class IHT(val size: Int) {
    val data = HashMap<ArrayList<Double>, Int>(size)

    fun tiles(numtilings: Int, floats: List<Double>, ints: List<Int>): List<Int> {
        val qfloats = DoubleArray(floats.size) { floor(floats[it] * numtilings) }
        val result = mutableListOf<Int>()
        for (tiling in 0 until numtilings) {
            val tilingX2 = tiling * 2
            val coords = ArrayList<Double>(1 + floats.size + ints.size)
            coords.add(tiling.toDouble())
            var b = tiling
            for (q in qfloats) {
                coords.add(floor(((q + b) / numtilings)))
                b += tilingX2
            }
            for (int in ints) {
                coords.add(int.toDouble())
            }
            result += data.getOrPut(coords, {
                if (data.size > size) print("error")
                data.size
            })
        }
        return result
    }
}

fun figure10_3() {
    val runs = 10
    val episodes = 500
    val numOfTilings = 8
    val alphas = listOf(0.5, 0.3)
    val nSteps = listOf(1, 8)

    val steps = Array(alphas.size) {
        IntArray(episodes)
    }
    for (run in 0 until runs) {
        val valueFunctions = Array(alphas.size) { ValueFunction(alphas[it], numOfTilings) }
        for ((index, valueFunction) in valueFunctions.withIndex())
            for (episode in 0 until episodes) {
                print("run: $run, steps: ${nSteps[index]}, episode: $episode")
                val step = semiGradientNStepSarsa(valueFunction, nSteps[index])
                println(", step=$step")
                steps[index][episode] += step
            }
    }
    val chart = chart("performance")
    for ((i, n) in nSteps.withIndex()) {
        val line = line("MountainCar episodic sarsa ($n) ")
        for (episode in 0 until episodes) {
            line[episode] = steps[i][episode] / runs.toDouble()
        }
        chart += line
    }
    ChartView.charts += chart
    Application.launch(ChartApp::class.java)
}

fun figure10_4() {
    val alphas = listOf(0.05, 0.1, 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4,1.6)
    val nSteps = listOf(1, 2, 4, 8, 16)
    val episodes = 50
    val runs = 5

    val chart = chart("performance")
    runBlocking {
        for (n in nSteps) {
            val line = line("n=$n ")
            for (alpha in alphas) {
                if ((n == 8 && alpha > 1) || (n == 16 && alpha > 0.75)) {
                    continue
                }
                val runChan = Channel<Int>(runs)
                repeat(runs) {
                    async {
                        val valueFunction = ValueFunction(alpha)
                        var step = 0
                        for (episode in 0 until episodes) {
                            step += semiGradientNStepSarsa(valueFunction, n)
                        }
                        runChan.send(step)
                    }
                }
                var step = 0
                repeat(runs) {
                    val _step = runChan.receive()
                    step += _step
                    println("alpha=$alpha n=$n run once")
                }

                line[alpha] = step / (runs * episodes).toDouble()
            }
            chart += line
            println("finish n=$n")
        }
    }
    ChartView.charts += chart
    Application.launch(ChartApp::class.java)
}

fun main(args: Array<String>) {
    figure10_4()
}