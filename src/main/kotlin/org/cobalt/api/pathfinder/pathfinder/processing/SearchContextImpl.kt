package org.cobalt.api.pathfinder.pathfinder.processing

import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration
import org.cobalt.api.pathfinder.pathing.context.EnvironmentContext
import org.cobalt.api.pathfinder.pathing.processing.context.SearchContext
import org.cobalt.api.pathfinder.provider.NavigationPointProvider
import org.cobalt.api.pathfinder.wrapper.PathPosition

class SearchContextImpl(
  private val startPathPosition: PathPosition,
  private val targetPathPosition: PathPosition,
  private val pathfinderConfiguration: PathfinderConfiguration,
  private val navigationPointProvider: NavigationPointProvider,
  private val environmentContext: EnvironmentContext?,
) : SearchContext {

  private val sharedData: MutableMap<String, Any> = HashMap()

  override fun getStartPathPosition(): PathPosition = startPathPosition

  override fun getTargetPathPosition(): PathPosition = targetPathPosition

  override fun getPathfinderConfiguration(): PathfinderConfiguration = pathfinderConfiguration

  override fun getNavigationPointProvider(): NavigationPointProvider = navigationPointProvider

  override fun getSharedData(): MutableMap<String, Any> = sharedData

  override fun getEnvironmentContext(): EnvironmentContext? = environmentContext
}
