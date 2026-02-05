package org.cobalt.api.pathfinder.pathing

import org.cobalt.api.pathfinder.wrapper.PathPosition

data class PathfindingProgress(
  private val start: PathPosition,
  private val current: PathPosition,
  private val target: PathPosition,
) {
  fun startPosition(): PathPosition = start
  fun currentPosition(): PathPosition = current
  fun targetPosition(): PathPosition = target
}
