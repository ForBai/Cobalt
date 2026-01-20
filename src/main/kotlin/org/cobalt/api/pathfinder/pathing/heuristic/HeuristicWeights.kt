package org.cobalt.api.pathfinder.pathing.heuristic

data class HeuristicWeights
private constructor(
  val manhattanWeight: Double,
  val octileWeight: Double,
  val perpendicularWeight: Double,
  val heightWeight: Double,
) {
  companion object {
    val DEFAULT_WEIGHTS = create(0.0, 1.0, 0.0, 0.0)

    fun create(
      manhattanWeight: Double,
      octileWeight: Double,
      perpendicularWeight: Double,
      heightWeight: Double,
    ): HeuristicWeights {
      return HeuristicWeights(manhattanWeight, octileWeight, perpendicularWeight, heightWeight)
    }
  }
}
