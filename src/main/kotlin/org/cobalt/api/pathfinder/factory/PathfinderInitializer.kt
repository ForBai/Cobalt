package org.cobalt.api.pathfinder.factory

import org.cobalt.api.pathfinder.pathing.Pathfinder
import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration

fun interface PathfinderInitializer {
  fun initialize(pathfinder: Pathfinder, configuration: PathfinderConfiguration)
}
