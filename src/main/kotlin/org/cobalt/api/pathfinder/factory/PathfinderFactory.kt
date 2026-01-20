package org.cobalt.api.pathfinder.factory

import org.cobalt.api.pathfinder.pathing.Pathfinder
import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration

interface PathfinderFactory {
  fun createPathfinder(): Pathfinder = createPathfinder(PathfinderConfiguration.DEFAULT)

  fun createPathfinder(configuration: PathfinderConfiguration): Pathfinder =
    throw UnsupportedOperationException(
      "This factory does not support creating pathfinders with a configuration."
    )

  fun createPathfinder(
    configuration: PathfinderConfiguration,
    initializer: PathfinderInitializer,
  ): Pathfinder {
    val pathfinder = createPathfinder(configuration)
    initializer.initialize(pathfinder, configuration)
    return pathfinder
  }
}
