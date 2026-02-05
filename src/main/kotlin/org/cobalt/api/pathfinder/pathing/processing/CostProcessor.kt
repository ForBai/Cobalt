package org.cobalt.api.pathfinder.pathing.processing

import org.cobalt.api.pathfinder.pathing.processing.context.EvaluationContext

interface CostProcessor : Processor {
  fun calculateCostContribution(context: EvaluationContext): Cost
}
