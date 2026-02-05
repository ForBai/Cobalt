package org.cobalt.api.pathfinder.pathing.hook

interface PathfinderHook {
  fun onPathfindingStep(pathfindingContext: PathfindingContext)
}
