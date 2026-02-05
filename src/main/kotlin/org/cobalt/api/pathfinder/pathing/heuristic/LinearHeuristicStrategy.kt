package org.cobalt.api.pathfinder.pathing.heuristic

import kotlin.math.abs
import kotlin.math.sqrt
import org.cobalt.api.pathfinder.pathing.calc.DistanceCalculator
import org.cobalt.api.pathfinder.wrapper.PathPosition

class LinearHeuristicStrategy : IHeuristicStrategy {
  companion object {
    private const val D1 = 1.0
    private val D2 = sqrt(2.0)
    private val D3 = sqrt(3.0)
  }

  private val perpendicularCalc =
    DistanceCalculator<Double> { progress ->
      InternalHeuristicUtils.calculatePerpendicularDistance(progress)
    }

  private val octileCalc =
    DistanceCalculator<Double> { progress ->
      val dx = abs(progress.currentPosition().flooredX - progress.targetPosition().flooredX)
      val dy = abs(progress.currentPosition().flooredY - progress.targetPosition().flooredY)
      val dz = abs(progress.currentPosition().flooredZ - progress.targetPosition().flooredZ)

      val min = minOf(dx, dy, dz)
      val max = maxOf(dx, dy, dz)
      val mid = dx + dy + dz - min - max

      (D3 - D2) * min + (D2 - D1) * mid + D1 * max
    }

  private val manhattanCalc =
    DistanceCalculator<Double> { progress ->
      val position = progress.currentPosition()
      val target = progress.targetPosition()

      (abs(position.flooredX - target.flooredX) +
        abs(position.flooredY - target.flooredY) +
        abs(position.flooredZ - target.flooredZ))
        .toDouble()
    }

  private val heightCalc =
    DistanceCalculator<Double> { progress ->
      val position = progress.currentPosition()
      val target = progress.targetPosition()

      abs(position.flooredY - target.flooredY).toDouble()
    }

  override fun calculate(context: HeuristicContext): Double {
    val progress = context.getPathfindingProgress()
    val weights = context.heuristicWeights()

    return manhattanCalc.calculate(progress)!! * weights.manhattanWeight +
      octileCalc.calculate(progress)!! * weights.octileWeight +
      perpendicularCalc.calculate(progress)!! * weights.perpendicularWeight +
      heightCalc.calculate(progress)!! * weights.heightWeight
  }

  override fun calculateTransitionCost(from: PathPosition, to: PathPosition): Double {
    val dx = to.centeredX - from.centeredX
    val dy = to.centeredY - from.centeredY
    val dz = to.centeredZ - from.centeredZ

    return sqrt(dx * dx + dy * dy + dz * dz)
  }
}
