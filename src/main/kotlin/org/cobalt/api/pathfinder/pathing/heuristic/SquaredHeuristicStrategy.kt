package org.cobalt.api.pathfinder.pathing.heuristic

import kotlin.math.abs
import kotlin.math.sqrt
import org.cobalt.api.pathfinder.pathing.calc.DistanceCalculator
import org.cobalt.api.pathfinder.wrapper.PathPosition

class SquaredHeuristicStrategy : IHeuristicStrategy {
  companion object {
    private const val D1 = 1.0
    private val D2 = sqrt(2.0)
    private val D3 = sqrt(3.0)
  }

  private val perpendicularCalc =
    DistanceCalculator { progress ->
      InternalHeuristicUtils.calculatePerpendicularDistanceSq(progress)
    }

  private val octileCalc =
    DistanceCalculator { progress ->
      val dx = abs(progress.currentPosition().flooredX - progress.targetPosition().flooredX)
      val dy = abs(progress.currentPosition().flooredY - progress.targetPosition().flooredY)
      val dz = abs(progress.currentPosition().flooredZ - progress.targetPosition().flooredZ)

      val min = minOf(dx, dy, dz)
      val max = maxOf(dx, dy, dz)
      val mid = dx + dy + dz - min - max

      val octile = (D3 - D2) * min + (D2 - D1) * mid + D1 * max
      octile * octile
    }

  private val manhattanCalc =
    DistanceCalculator { progress ->
      val c = progress.currentPosition()
      val t = progress.targetPosition()

      val manhattan =
        abs(c.flooredX - t.flooredX) +
          abs(c.flooredY - t.flooredY) +
          abs(c.flooredZ - t.flooredZ)

      (manhattan * manhattan).toDouble()
    }

  private val heightCalc =
    DistanceCalculator { progress ->
      val dy = progress.currentPosition().flooredY - progress.targetPosition().flooredY
      (dy * dy).toDouble()
    }

  override fun calculate(context: HeuristicContext): Double {
    val p = context.getPathfindingProgress()
    val w = context.heuristicWeights()

    return manhattanCalc.calculate(p)!! * w.manhattanWeight +
      octileCalc.calculate(p)!! * w.octileWeight +
      perpendicularCalc.calculate(p)!! * w.perpendicularWeight +
      heightCalc.calculate(p)!! * w.heightWeight
  }

  override fun calculateTransitionCost(from: PathPosition, to: PathPosition): Double {
    val dx = to.centeredX - from.centeredX
    val dy = to.centeredY - from.centeredY
    val dz = to.centeredZ - from.centeredZ

    return dx * dx + dy * dy + dz * dz
  }
}
