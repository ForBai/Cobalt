package org.cobalt.api.pathfinder.factory

import org.cobalt.api.pathfinder.pathing.Pathfinder
import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration

interface PathfinderFactory {
  fun createPathfinder(): Pathfinder
  fun createPathfinder(configuration: PathfinderConfiguration): Pathfinder
  fun createPathfinder(configuration: PathfinderConfiguration, initializer: PathfinderInitializer): Pathfinder {
    val pathfinder = createPathfinder(configuration)
    initializer.initialize(pathfinder, configuration)
    return pathfinder
  }
}
