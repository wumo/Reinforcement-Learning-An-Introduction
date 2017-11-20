# Reinforcement Learning: An Introduction

[Kotlin](https://kotlinlang.org/) implementation of algorithms, examples, and exercises from the [Sutton and Barto: Reinforcement Learning (2nd Edition)](http://incompleteideas.net/sutton/book/bookdraft2017nov5.pdf). The purpose of this project is to help understanding RL algorithms and experimenting easily. 

Inspired by [ShangtongZhang/reinforcement-learning-an-introduction (Python)](https://github.com/ShangtongZhang/reinforcement-learning-an-introduction)
and [idsc-frazzoli/subare (Java 8)](https://github.com/idsc-frazzoli/subare)

## Implemented algorithms:
Model-based (Dynamic Programming):
* [Policy Iteration (Action-Value Iteration)](src/main/kotlin/lab/mars/rl/algo/dp/PolicyIteration.kt) (p.65)
* [Value Iteration](src/main/kotlin/lab/mars/rl/algo/dp/ValueIteration.kt)  (p.67)

Monte Carlo (episode backup):
* [First-visit MC prediction](src/main/kotlin/lab/mars/rl/algo/mc/Prediction.kt) (p.76)
* [Monte Carlo Exploring Starts](src/main/kotlin/lab/mars/rl/algo/mc/ExploringStarts.kt) (p.81)
* [On-Policy first-visit MC control](src/main/kotlin/lab/mars/rl/algo/mc/On-Policy%20Optimal.kt) (p.83)
* [Off-policy MC prediction](src/main/kotlin/lab/mars/rl/algo/mc/Off-Policy%20Prediction.kt) (p.90)
* [Off-policy MC control](src/main/kotlin/lab/mars/rl/algo/mc/Off-policy%20Optimal.kt) (p.91)

Temporal Difference (one-step backup):
* [Tabular TD(0)](src/main/kotlin/lab/mars/rl/algo/td/Prediction.kt) (p.98)
* [Sarsa](src/main/kotlin/lab/mars/rl/algo/td/Sarsa.kt) (p.106)
* [Q-learning](src/main/kotlin/lab/mars/rl/algo/td/QLearning.kt) (p.107)
* [Expected Sarsa](src/main/kotlin/lab/mars/rl/algo/td/ExpectedSarsa.kt) (p.109)
* [Double Q-Learning](src/main/kotlin/lab/mars/rl/algo/td/DoubleQLearning.kt) (p.111)

n-step Temporal Difference (unify MC and TD):
* [n-step TD prediction](src/main/kotlin/lab/mars/rl/algo/ntd/Prediction.kt) (p.117)
* [n-step Sarsa](src/main/kotlin/lab/mars/rl/algo/ntd/Sarsa.kt) (p.120)
* [Off-policy n-step Sarsa](src/main/kotlin/lab/mars/rl/algo/ntd/Off-policy%20Sarsa.kt) (p.122)
* [n-step Tree Backup](src/main/kotlin/lab/mars/rl/algo/ntd/Treebackup.kt) (p.125)
* [Off-policy n-step Q(σ)](src/main/kotlin/lab/mars/rl/algo/ntd/Off-policy%20Q%20sigma.kt) (p.128)

Dyna (Integrate Planning, Acting, and Learning):
* [Random-sample one-step tabular Q-planning](src/main/kotlin/lab/mars/rl/algo/dyna/RandomSampleOneStepTabularQLearning.kt) (p.133)
* [Tabular Dyna-Q](src/main/kotlin/lab/mars/rl/algo/dyna/Dyna-Q.kt) (p.135)
* [Tabular Dyna-Q+](src/main/kotlin/lab/mars/rl/algo/dyna/Dyna-Q+.kt) (p.138)
* [Prioritized Sweeping](src/main/kotlin/lab/mars/rl/algo/dyna/PrioritizedSweeping.kt) (p.140)
* [Prioritized Sweeping Stochastic Environment](src/main/kotlin/lab/mars/rl/algo/dyna/PrioritizedSweepingStochasticEnv.kt) (p.141)

On-policy Prediction with Function Approximation
* [Gradient Monte Carlo algorithm](src/main/kotlin/lab/mars/rl/algo/func_approx/Gradient%20Monte%20Carlo%20algorithm.kt) (p.165)
* [Semi-gradient TD(0)](src/main/kotlin/lab/mars/rl/algo/func_approx/Semi-gradient%20TD(0).kt) (p.166)
* [n-step semi-gradient TD](src/main/kotlin/lab/mars/rl/algo/func_approx/n-step%20semi-gradient%20TD.kt) (p.171)
* [Least-Squares TD](src/main/kotlin/lab/mars/rl/algo/func_approx/LSTD.kt) (p.186)

Eligibility Traces
* [Semi-gradient TD(λ) prediction](src/main/kotlin/lab/mars/rl/algo/eligibility_trace/Semi-gradient%20TD(λ)%20prediction.kt) (p.240)
* [True Online TD(λ) prediction](src/main/kotlin/lab/mars/rl/algo/eligibility_trace/True%20Online%20TD(λ)%20prediction.kt) (p.246)

## Implemented problems:
* [Grid world](src/main/kotlin/lab/mars/rl/problem/GridWorld.kt) (p.61)
* [Jack's Car Rental and exercise 4.4](src/main/kotlin/lab/mars/rl/problem/CarRental.kt) (p.65)
* [Gambler's Problem](src/main/kotlin/lab/mars/rl/problem/Gambler.kt) (p.68)
* [Blackjack](src/main/kotlin/lab/mars/rl/problem/Blackjack.kt) (p.76)
* [Random Walk](src/main/kotlin/lab/mars/rl/problem/RandomWalk.kt) (p.102)
* [Windy Gridworld and King's Moves](src/main/kotlin/lab/mars/rl/problem/WindyGridworld.kt) (p.106)
* [Cliff Walking](src/main/kotlin/lab/mars/rl/problem/CliffWalking.kt) (p.108)
* [Maximization Bias Example](src/main/kotlin/lab/mars/rl/problem/MaximizationBias.kt) (p.110)
* [Dyna Maze](src/main/kotlin/lab/mars/rl/problem/DynaMaze.kt) (p.136)
* [Rod Maneuvering](src/main/kotlin/lab/mars/rl/problem/RodManeuvering.kt) (p.141)
* [1000-state Random Walk](src/main/kotlin/lab/mars/rl/problem/1000-state%20RandomWalk.kt) (p.166)

## Running
Built with [Maven](https://maven.apache.org/) 

Try [Testcases](src/test/kotlin/lab/mars/rl/model/impl)

