# Reinforcement Learning: An Introduction

Kotlin implementation of algorithms, examples, and exercises from the [Sutton and Barto: Reinforcement Learning 2nd](http://incompleteideas.net/sutton/book/bookdraft2017june19.pdf)

Inspired by [ShangtongZhang/reinforcement-learning-an-introduction](https://github.com/ShangtongZhang/reinforcement-learning-an-introduction)
and [idsc-frazzoli/subare](https://github.com/idsc-frazzoli/subare)

Implemented algorithms:

* [Policy Iteration](src/main/kotlin/lab/mars/rl/algo/PolicyIteration.kt) (p.89)
* [Value Iteration](src/main/kotlin/lab/mars/rl/algo/ValueIteration.kt) (Action-Value Iteration) (p.92)

MC:

* [First-visit MC prediction](src/main/kotlin/lab/mars/rl/algo/mc/Prediction.kt) (p.102)
* [Monte Carlo Exploring Starts](src/main/kotlin/lab/mars/rl/algo/mc/ExploringStarts.kt) (p.109)
* [On-Policy first-visit MC control](src/main/kotlin/lab/mars/rl/algo/mc/On-Policy%20Optimal.kt) (p.111)
* [Off-policy MC prediction](src/main/kotlin/lab/mars/rl/algo/mc/Off-Policy%20Prediction.kt) (p.120)
* [Off-policy MC control](src/main/kotlin/lab/mars/rl/algo/mc/Off-policy%20Optimal.kt) (p.121)

TD:
* [Tabular TD(0)](src/main/kotlin/lab/mars/rl/algo/td/Prediction.kt) (p.130)
* [Sarsa](src/main/kotlin/lab/mars/rl/algo/td/Sarsa.kt) (p.140)
* [Q-learning](src/main/kotlin/lab/mars/rl/algo/td/QLearning.kt) (p.142)
* [Expected Sarsa](src/main/kotlin/lab/mars/rl/algo/td/ExpectedSarsa.kt) (p.144)
* [Double Q-Learning](src/main/kotlin/lab/mars/rl/algo/td/DoubleQLearning.kt) (p.147)

n-step TD:
* [n-step TD prediction](src/main/kotlin/lab/mars/rl/algo/ntd/Prediction.kt) (p.156)
* [n-step Sarsa](src/main/kotlin/lab/mars/rl/algo/ntd/Sarsa.kt) (p.159)
* [Off-policy n-step Sarsa](src/main/kotlin/lab/mars/rl/algo/ntd/Off-policy%20Sarsa.kt) (p.161)
* [n-step Tree Backup](src/main/kotlin/lab/mars/rl/algo/ntd/Treebackup.kt) (p.165)
* [Off-policy n-step Q(σ)](src/main/kotlin/lab/mars/rl/algo/ntd/Off-policy%20Q%20sigma.kt) (p.169)

Dyna:
* [Random-sample one-step tabular Q-planning](src/main/kotlin/lab/mars/rl/algo/dyna/RandomSampleOneStepTabularQLearning.kt) (p.175)
* [Tabular Dyna-Q](src/main/kotlin/lab/mars/rl/algo/dyna/Dyna-Q.kt) (p.178)
* [Tabular Dyna-Q+](src/main/kotlin/lab/mars/rl/algo/dyna/Dyna-Q+.kt) (p.182)
* Prioritized Sweeping (from p.178)

Implemented problems:
* Grid world (p.84)
* Jack's Car Rental and exercise 4.4 (p.89)
* Gambler's Problem (p.92)
* Blackjack (p.103)
* Random Walk (p.135)
* Windy Gridworld and King's Moves (p.140)
* Cliff Walking (p.142)
* Maximization Bias Example (p.145)
* Dyna Maze (p.178)



