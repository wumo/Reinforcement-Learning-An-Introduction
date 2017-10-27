# Reinforcement Learning: An Introduction

Kotlin implementation of algorithms, examples, and exercises from the [Sutton and Barto: Reinforcement Learning 2nd](http://incompleteideas.net/sutton/book/bookdraft2017june19.pdf)

Inspired by [ShangtongZhang/reinforcement-learning-an-introduction](https://github.com/ShangtongZhang/reinforcement-learning-an-introduction)
and [idsc-frazzoli/subare](https://github.com/idsc-frazzoli/subare)

Implemented algorithms:

* Policy Iteration (p.89)[kotlin](src/main/kotlin/lab/mars/rl/algo/PolicyIteration.kt)
* Value Iteration (Action-Value Iteration) (p.92)

MC:

* First-visit MC prediction (p.102)
* Monte Carlo Exploring Starts (p.109)
* On-Policy first-visit MC control (p.111)
* Off-policy MC prediction (p.120)
* Off-policy MC control (p.121)

TD:
* Tabular TD(0) (p.130)
* Sarsa (p.140)
* Q-learning(p.142)
* Expected Sarsa (p.144)
* Double Q-Learning (p.147)

n-step TD:
* n-step TD prediction (p.156)
* n-step Sarsa (p.159)
* Off-policy n-step Sarsa (p.161)
* n-step Tree Backup (p.165)
* Off-policy n-step Q(σ) (p.169)

Dyna:
* Random-sample one-step tabular Q-planning (p.175)
* Tabular Dyna-Q (p.178)
* Tabular Dyna-Q+ (p.182)
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



