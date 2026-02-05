package org.cobalt.api.pathfinder.pathfinder.processing

import kotlin.math.max
import org.cobalt.api.pathfinder.Node
import org.cobalt.api.pathfinder.pathing.heuristic.IHeuristicStrategy
import org.cobalt.api.pathfinder.pathing.processing.context.EvaluationContext
import org.cobalt.api.pathfinder.pathing.processing.context.SearchContext
import org.cobalt.api.pathfinder.wrapper.PathPosition

class EvaluationContextImpl(
  private val searchContext: SearchContext,
  private val engineNode: Node,
  private val parentEngineNode: Node?,
  private val heuristicStrategy: IHeuristicStrategy,
) : EvaluationContext {

  override fun getCurrentPathPosition(): PathPosition = engineNode.getPosition()

  override fun getPreviousPathPosition(): PathPosition? = parentEngineNode?.getPosition()

  override fun getCurrentNodeDepth(): Int = engineNode.getDepth()

  override fun getCurrentNodeHeuristicValue(): Double = engineNode.getHeuristic()

  override fun getPathCostToPreviousPosition(): Double {
    return parentEngineNode?.getGCost() ?: 0.0
  }

  override fun getBaseTransitionCost(): Double {
    if (parentEngineNode == null) return 0.0

    val from = parentEngineNode.getPosition()
    val to = engineNode.getPosition()
    val baseCost = heuristicStrategy.calculateTransitionCost(from, to)

    if (baseCost.isNaN() || baseCost.isInfinite()) {
      throw IllegalStateException(
        "Heuristic transition cost produced an invalid numeric value: $baseCost"
      )
    }

    return max(baseCost, 0.0)
  }

  override fun getSearchContext(): SearchContext = searchContext

  override fun getGrandparentPathPosition(): PathPosition? =
    parentEngineNode?.getParent()?.getPosition()
}
