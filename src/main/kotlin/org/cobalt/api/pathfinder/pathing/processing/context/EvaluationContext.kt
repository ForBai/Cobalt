package org.cobalt.api.pathfinder.pathing.processing.context

import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration
import org.cobalt.api.pathfinder.pathing.context.EnvironmentContext
import org.cobalt.api.pathfinder.provider.NavigationPointProvider
import org.cobalt.api.pathfinder.wrapper.PathPosition

interface EvaluationContext {
  fun getCurrentPathPosition(): PathPosition
  fun getPreviousPathPosition(): PathPosition?
  fun getCurrentNodeDepth(): Int
  fun getCurrentNodeHeuristicValue(): Double
  fun getPathCostToPreviousPosition(): Double
  fun getBaseTransitionCost(): Double
  fun getSearchContext(): SearchContext
  fun getGrandparentPathPosition(): PathPosition?

  fun getPathfinderConfiguration(): PathfinderConfiguration {
    return getSearchContext().getPathfinderConfiguration()
  }

  fun getNavigationPointProvider(): NavigationPointProvider {
    return getSearchContext().getNavigationPointProvider()
  }

  fun getSharedData(): MutableMap<String, Any> {
    return getSearchContext().getSharedData()
  }

  fun getStartPathPosition(): PathPosition {
    return getSearchContext().getStartPathPosition()
  }

  fun getTargetPathPosition(): PathPosition {
    return getSearchContext().getTargetPathPosition()
  }

  fun getEnvironmentContext(): EnvironmentContext? {
    return getSearchContext().getEnvironmentContext()
  }
}
