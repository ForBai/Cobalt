package org.cobalt.api.pathfinder.pathing.processing.context

import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration
import org.cobalt.api.pathfinder.pathing.context.EnvironmentContext
import org.cobalt.api.pathfinder.provider.NavigationPointProvider
import org.cobalt.api.pathfinder.wrapper.PathPosition

interface SearchContext {
  fun getStartPathPosition(): PathPosition
  fun getTargetPathPosition(): PathPosition
  fun getPathfinderConfiguration(): PathfinderConfiguration
  fun getNavigationPointProvider(): NavigationPointProvider
  fun getSharedData(): MutableMap<String, Any>
  fun getEnvironmentContext(): EnvironmentContext?
}
