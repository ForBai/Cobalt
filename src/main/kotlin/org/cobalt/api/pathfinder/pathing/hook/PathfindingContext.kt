package org.cobalt.api.pathfinder.pathing.hook

import org.cobalt.api.pathfinder.wrapper.Depth
import org.cobalt.api.pathfinder.wrapper.PathPosition

data class PathfindingContext(
  private val currentPosition: PathPosition,
  private val depth: Depth,
) {
  fun currentPosition(): PathPosition = currentPosition
  fun getDepth(): Depth = depth
}
