package org.cobalt.api.pathfinder.pathing.processing.impl

import kotlin.math.sqrt
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import org.cobalt.api.pathfinder.pathing.processing.Cost
import org.cobalt.api.pathfinder.pathing.processing.CostProcessor
import org.cobalt.api.pathfinder.pathing.processing.ValidationProcessor
import org.cobalt.api.pathfinder.pathing.processing.context.EvaluationContext

/*
 * most logic in this file is derived from minecraft code
 * or writeups on pathfinding algorithms, if you want to help contribute
 * id prefer for you to keep it the same idea or whatever, but if not
 * please write a comment explaining WHY you did it that way. i dont like
 * magic numbers that i cant understand.
 */
class MinecraftPathProcessor : CostProcessor, ValidationProcessor {

  private val mc: Minecraft = Minecraft.getInstance()

  companion object {
    private const val DEFAULT_MOB_JUMP_HEIGHT = 1.125 // WalkNodeEvaluator
  }

  override fun isValid(context: EvaluationContext): Boolean {
    val provider = context.getSearchContext().getNavigationPointProvider()
    val pos = context.getCurrentPathPosition()
    val prev = context.getPreviousPathPosition()
    val env = context.getSearchContext().getEnvironmentContext()

    val currentPoint = provider.getNavigationPoint(pos, env)

    if (!currentPoint.isTraversable()) return false
    if (prev == null) return true

    val prevPoint = provider.getNavigationPoint(prev, env)
    val dy = pos.y - prev.y
    val dx = pos.flooredX - prev.flooredX
    val dz = pos.flooredZ - prev.flooredZ

    if (dy > DEFAULT_MOB_JUMP_HEIGHT) return false

    if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
      val corner1Pos = prev.add(dx.toDouble(), 0.0, 0.0)
      val corner2Pos = prev.add(0.0, 0.0, dz.toDouble())
      val c1Point = provider.getNavigationPoint(corner1Pos, env)
      val c2Point = provider.getNavigationPoint(corner2Pos, env)

      // node3.y <= node.y && node2.y <= node.y
      if (!c1Point.isTraversable() || !c2Point.isTraversable()) return false
    }

    return when {
      dy < -0.5 -> true // falling
      dy > 0.5 ->
        prevPoint.hasFloor() ||
          currentPoint.isClimbable() ||
          currentPoint.isLiquid() // jumping/climbing
      else ->
        currentPoint.hasFloor() ||
          prevPoint.hasFloor() ||
          currentPoint.isClimbable() ||
          prevPoint.isClimbable() ||
          currentPoint.isLiquid() ||
          prevPoint.isLiquid()
    }
  }

  override fun calculateCostContribution(context: EvaluationContext): Cost {
    val level = mc.level ?: return Cost.ZERO
    val currentPos = context.getCurrentPathPosition()
    val prevPos = context.getPreviousPathPosition() ?: return Cost.ZERO
    val provider = context.getSearchContext().getNavigationPointProvider()
    val env = context.getSearchContext().getEnvironmentContext()

    val currentPoint = provider.getNavigationPoint(currentPos, env)
    val prevPoint = provider.getNavigationPoint(prevPos, env)

    val dy = currentPoint.getFloorLevel() - prevPoint.getFloorLevel()
    var additionalCost = 0.0

    if (dy > 0.1) {
      additionalCost += 0.5 * dy
    } else if (dy < -0.1) {
      additionalCost += 0.1 * Math.abs(dy)
    }

    val blockPos = BlockPos(currentPos.flooredX, currentPos.flooredY, currentPos.flooredZ)

    // i dont want it to like tight corners so more cost
    var crampedPenalty = 0.0
    for (i in 2..3) {
      if (level.getBlockState(blockPos.above(i)).canOcclude()) {
        crampedPenalty += 0.1 / i.toDouble()
      }
    }
    if (level.getBlockState(blockPos.west()).canOcclude() ||
      level.getBlockState(blockPos.east()).canOcclude() ||
      level.getBlockState(blockPos.north()).canOcclude() ||
      level.getBlockState(blockPos.south()).canOcclude()
    ) {
      crampedPenalty += 0.05
    }
    additionalCost += crampedPenalty

    // just make stuff smoother no more zigzags
    val gpPos = context.getGrandparentPathPosition()
    if (gpPos != null) {
      val v1x = prevPos.x - gpPos.x
      val v1z = prevPos.z - gpPos.z
      val v2x = currentPos.x - prevPos.x
      val v2z = currentPos.z - prevPos.z
      val dot = v1x * v2x + v1z * v2z
      val mag1 = sqrt(v1x * v1x + v1z * v1z)
      val mag2 = sqrt(v2x * v2x + v2z * v2z)
      if (mag1 > 0.1 && mag2 > 0.1) {
        val normalizedDot = dot / (mag1 * mag2)
        if (normalizedDot < 0.99) additionalCost += 0.05
      }
    }

    return Cost.of(additionalCost)
  }
}
