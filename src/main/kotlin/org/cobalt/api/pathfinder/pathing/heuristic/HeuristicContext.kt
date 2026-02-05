package org.cobalt.api.pathfinder.pathing.heuristic

import org.cobalt.api.pathfinder.pathing.PathfindingProgress
import org.cobalt.api.pathfinder.wrapper.PathPosition

class HeuristicContext {
  private val pathfindingProgress: PathfindingProgress
  private val heuristicWeights: HeuristicWeights

  constructor(
    position: PathPosition,
    startPosition: PathPosition,
    targetPosition: PathPosition,
    heuristicWeights: HeuristicWeights,
  ) {
    this.pathfindingProgress = PathfindingProgress(startPosition, position, targetPosition)
    this.heuristicWeights = heuristicWeights
  }

  constructor(pathfindingProgress: PathfindingProgress, heuristicWeights: HeuristicWeights) {
    this.pathfindingProgress = pathfindingProgress
    this.heuristicWeights = heuristicWeights
  }

  fun getPathfindingProgress(): PathfindingProgress = pathfindingProgress

  fun position(): PathPosition = pathfindingProgress.currentPosition()

  fun startPosition(): PathPosition = pathfindingProgress.startPosition()

  fun targetPosition(): PathPosition = pathfindingProgress.targetPosition()

  fun heuristicWeights(): HeuristicWeights = heuristicWeights
}
