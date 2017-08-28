##  EvoBugs: free evolution in a minimal environment

A population of mobile agents controlled by neural networks reproduce and
evolve autonomously in a 2D environment. This model is inspired by Alastair
Channon's [Geb](http://www.channon.net/alastair/) and Larry Yaeger's
[Polyworld](https://en.wikipedia.org/wiki/Polyworld), and can largely be
regarded as a simplification of both.

Important  features: 

* Agents have a certain amount of energy, which is replenished by ingesting
  food, or killing other agents (see below) and decreased by moving and
  attacking, as well as decaying at a constant rate over time. When energy goes
  below zero, the agent dies.

* Reproduction is automatic: after an agent has survived for a certain minimum
  time, it will automatically reproduce with a fixed, low probability at every
  time step. Thus, all that is needed to reproduce is to survive. Reproduction
  is asexual, without crossover, but with random mutation.

* All agents have controllers for speed and direction. Agents may also have
  sensors for food bits and other agents, as well as controllers for
  aggression. However, these are optional: there is an energy cost for the
  total number of neurons, including sensors and actuators.

* Agents with an "aggression" controller can exhibit agggression, which allow
  them to attack (and defend themselves from) each other. If two agents come in
  contact, the agent with the lowest current level of aggression sees its energy
  decrease by the difference between the two agent's aggression levels (thus,
  aggression can be both offensive and defensive, as it can nullify another
  agent's aggression)

* If one agent dies as a result of another agent's attack, the attacking agent
  receives a large energy bonus.

* Energy cost grows with the *square* of speed, aggression, and number of
  neurons, forcing sharp cost-benefit trade-offs.

* The world receives a constant influx of food bits (the food bits also move);
  thus, the world has a roughly constant energy *flux*, rather than
  conservaation of total energy

* Because of this, the difficulty of survival is automatically adapted to the
  current population: initially, even random agents can still survive (and thus reproduce), beause
  the food accumulates so much that even random motion allows for survival;
  however,  after some more competitive behaviors emerge (e.g. ability to move
  towards food bits), food rarefies, and poor agents with sub-optimal wiring
  cannot survive anymore. 

* After a while, depending on initial parameters, the world undergoes
  speciation between a large population of "herbivores" (specialized for
  chasing after food bits) and "carnivores" (which can eat food bits, but also
  pursue and attack other agents).
