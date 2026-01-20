package org.cobalt.api.pathfinder.pathing.processing

import org.cobalt.api.pathfinder.pathing.processing.context.EvaluationContext

interface ValidationProcessor : Processor {
  fun isValid(context: EvaluationContext): Boolean
}
