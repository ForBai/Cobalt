package org.cobalt.api.pathfinder.pathing.calc

import org.cobalt.api.pathfinder.pathing.PathfindingProgress

fun interface DistanceCalculator<M> {
  fun calculate(progress: PathfindingProgress): M?
}
