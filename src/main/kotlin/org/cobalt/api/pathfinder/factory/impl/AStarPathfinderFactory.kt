package org.cobalt.api.pathfinder.factory.impl

import org.cobalt.api.pathfinder.factory.PathfinderFactory
import org.cobalt.api.pathfinder.pathfinder.AStarPathfinder
import org.cobalt.api.pathfinder.pathing.Pathfinder
import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration

class AStarPathfinderFactory : PathfinderFactory {

  override fun createPathfinder(): Pathfinder {
    return AStarPathfinder(PathfinderConfiguration.builder().build())
  }

  override fun createPathfinder(configuration: PathfinderConfiguration): Pathfinder {
    return AStarPathfinder(configuration)
  }
}
