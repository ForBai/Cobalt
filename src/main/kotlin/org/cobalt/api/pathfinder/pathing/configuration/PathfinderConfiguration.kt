package org.cobalt.api.pathfinder.pathing.configuration

import org.cobalt.api.pathfinder.pathing.INeighborStrategy
import org.cobalt.api.pathfinder.pathing.NeighborStrategies
import org.cobalt.api.pathfinder.pathing.context.EnvironmentContext
import org.cobalt.api.pathfinder.pathing.heuristic.HeuristicStrategies
import org.cobalt.api.pathfinder.pathing.heuristic.HeuristicWeights
import org.cobalt.api.pathfinder.pathing.heuristic.IHeuristicStrategy
import org.cobalt.api.pathfinder.pathing.processing.CostProcessor
import org.cobalt.api.pathfinder.pathing.processing.ValidationProcessor
import org.cobalt.api.pathfinder.provider.NavigationPoint
import org.cobalt.api.pathfinder.provider.NavigationPointProvider
import org.cobalt.api.pathfinder.wrapper.PathPosition

data class PathfinderConfiguration(
  val maxIterations: Int,
  val maxLength: Int,
  val async: Boolean,
  val fallback: Boolean,
  val provider: NavigationPointProvider,
  val heuristicWeights: HeuristicWeights,
  val validationProcessors: List<ValidationProcessor>,
  val costProcessors: List<CostProcessor>,
  val neighborStrategy: INeighborStrategy,
  val gridCellSize: Int,
  val bloomFilterSize: Int,
  val bloomFilterFpp: Double,
  val heuristicStrategy: IHeuristicStrategy,
  val reopenClosedNodes: Boolean,
) {
  companion object {
    fun deepCopy(pathfinderConfiguration: PathfinderConfiguration): PathfinderConfiguration {
      return builder()
        .maxIterations(pathfinderConfiguration.maxIterations)
        .maxLength(pathfinderConfiguration.maxLength)
        .async(pathfinderConfiguration.async)
        .fallback(pathfinderConfiguration.fallback)
        .provider(pathfinderConfiguration.provider)
        .heuristicWeights(pathfinderConfiguration.heuristicWeights)
        .nodeValidationProcessors(pathfinderConfiguration.validationProcessors)
        .nodeCostProcessors(pathfinderConfiguration.costProcessors)
        .neighborStrategy(pathfinderConfiguration.neighborStrategy)
        .gridCellSize(pathfinderConfiguration.gridCellSize)
        .bloomFilterSize(pathfinderConfiguration.bloomFilterSize)
        .bloomFilterFpp(pathfinderConfiguration.bloomFilterFpp)
        .heuristicStrategy(pathfinderConfiguration.heuristicStrategy)
        .reopenClosedNodes(pathfinderConfiguration.reopenClosedNodes)
        .build()
    }

    fun builder(): PathfinderConfigurationBuilder {
      return PathfinderConfigurationBuilder()
    }
  }

  fun getNodeCostProcessors(): List<CostProcessor> = costProcessors

  fun getNodeValidationProcessors(): List<ValidationProcessor> = validationProcessors

  fun shouldReopenClosedNodes(): Boolean = reopenClosedNodes
}

class PathfinderConfigurationBuilder {
  private var maxIterations: Int = 5000
  private var maxLength: Int = 0
  private var async: Boolean = false
  private var fallback: Boolean = true
  private var provider: NavigationPointProvider =
    object : NavigationPointProvider {
      override fun getNavigationPoint(
        position: PathPosition,
        environmentContext: EnvironmentContext?,
      ): NavigationPoint {
        return object : NavigationPoint {
          override fun isTraversable(): Boolean = true
          override fun hasFloor(): Boolean = true
          override fun isClimbable(): Boolean = false
          override fun isLiquid(): Boolean = false
        }
      }
    }
  private var heuristicWeights: HeuristicWeights = HeuristicWeights.DEFAULT_WEIGHTS
  private var validationProcessors: List<ValidationProcessor> = emptyList()
  private var costProcessors: List<CostProcessor> = emptyList()
  private var neighborStrategy: INeighborStrategy = NeighborStrategies.VERTICAL_AND_HORIZONTAL
  private var gridCellSize: Int = 12
  private var bloomFilterSize: Int = 1000
  private var bloomFilterFpp: Double = 0.01
  private var heuristicStrategy: IHeuristicStrategy = HeuristicStrategies.LINEAR
  private var reopenClosedNodes: Boolean = false

  fun maxIterations(maxIterations: Int): PathfinderConfigurationBuilder {
    this.maxIterations = maxIterations
    return this
  }

  fun maxLength(maxLength: Int): PathfinderConfigurationBuilder {
    this.maxLength = maxLength
    return this
  }

  fun async(async: Boolean): PathfinderConfigurationBuilder {
    this.async = async
    return this
  }

  fun fallback(allowingFallback: Boolean): PathfinderConfigurationBuilder {
    this.fallback = allowingFallback
    return this
  }

  fun provider(provider: NavigationPointProvider): PathfinderConfigurationBuilder {
    this.provider = provider
    return this
  }

  fun heuristicWeights(heuristicWeights: HeuristicWeights): PathfinderConfigurationBuilder {
    this.heuristicWeights = heuristicWeights
    return this
  }

  fun nodeValidationProcessors(
    validationProcessors: List<ValidationProcessor>,
  ): PathfinderConfigurationBuilder {
    this.validationProcessors = validationProcessors
    return this
  }

  fun nodeCostProcessors(costProcessors: List<CostProcessor>): PathfinderConfigurationBuilder {
    this.costProcessors = costProcessors
    return this
  }

  fun neighborStrategy(neighborStrategy: INeighborStrategy): PathfinderConfigurationBuilder {
    this.neighborStrategy = neighborStrategy
    return this
  }

  fun gridCellSize(gridCellSize: Int): PathfinderConfigurationBuilder {
    this.gridCellSize = gridCellSize
    return this
  }

  fun bloomFilterSize(bloomFilterSize: Int): PathfinderConfigurationBuilder {
    this.bloomFilterSize = bloomFilterSize
    return this
  }

  fun bloomFilterFpp(bloomFilterFpp: Double): PathfinderConfigurationBuilder {
    this.bloomFilterFpp = bloomFilterFpp
    return this
  }

  fun heuristicStrategy(heuristicStrategy: IHeuristicStrategy): PathfinderConfigurationBuilder {
    this.heuristicStrategy = heuristicStrategy
    return this
  }

  fun reopenClosedNodes(reopenClosedNodes: Boolean): PathfinderConfigurationBuilder {
    this.reopenClosedNodes = reopenClosedNodes
    return this
  }

  fun build(): PathfinderConfiguration {
    return PathfinderConfiguration(
      maxIterations = this.maxIterations,
      maxLength = this.maxLength,
      async = this.async,
      fallback = this.fallback,
      provider = this.provider,
      heuristicWeights = this.heuristicWeights,
      validationProcessors = this.validationProcessors.toList(),
      costProcessors = this.costProcessors.toList(),
      neighborStrategy = this.neighborStrategy,
      gridCellSize = this.gridCellSize,
      bloomFilterSize = this.bloomFilterSize,
      bloomFilterFpp = this.bloomFilterFpp,
      heuristicStrategy = this.heuristicStrategy,
      reopenClosedNodes = this.reopenClosedNodes
    )
  }
}
