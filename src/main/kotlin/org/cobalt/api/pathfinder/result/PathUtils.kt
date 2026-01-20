package org.cobalt.api.pathfinder.result

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt
import net.minecraft.util.Mth
import org.cobalt.api.pathfinder.pathing.result.Path
import org.cobalt.api.pathfinder.util.ParameterizedSupplier
import org.cobalt.api.pathfinder.wrapper.PathPosition

object PathUtils {

  fun interpolate(path: Path, resolution: Double): Path {
    require(resolution > 0) { "Resolution must be > 0" }

    val result = ArrayDeque<PathPosition>()
    var previous: PathPosition? = null

    for (current in path) {
      previous?.let { prev ->
        interpolateSegment(prev, current, resolution, result)
      }
      result.addLast(current)
      previous = current
    }

    return buildPath(result)
  }

  fun simplify(path: Path, epsilon: Double): Path {
    validateEpsilon(epsilon)

    val result = ArrayDeque<PathPosition>()
    var index = 0
    val stride = maxOf(1, (1.0 / epsilon).roundToInt())

    for (pos in path) {
      if (index % stride == 0) {
        result.addLast(pos)
      }
      index++
    }

    return buildPath(result)
  }

  fun join(first: Path, second: Path): Path {
    if (first.length() == 0) return second
    if (second.length() == 0) return first

    val result = ArrayDeque<PathPosition>()
    first.forEach { result.addLast(it) }
    second.forEach { result.addLast(it) }

    return buildPath(result)
  }

  fun trim(path: Path, maxLength: Int): Path {
    require(maxLength > 0) { "maxLength must be > 0" }

    if (path.length() <= maxLength) return path

    val result = ArrayDeque<PathPosition>()
    var count = 0

    for (p in path) {
      result.addLast(p)
      if (++count >= maxLength) break
    }

    return buildPath(result)
  }

  fun mutatePositions(path: Path, mutator: ParameterizedSupplier<PathPosition>): Path {
    val result = ArrayDeque<PathPosition>(path.length())

    for (pos in path) {
      result.addLast(mutator.accept(pos))
    }

    return buildPath(result)
  }

  private fun interpolateSegment(
    start: PathPosition,
    end: PathPosition,
    resolution: Double,
    result: ArrayDeque<PathPosition>,
  ) {
    val distance = start.distance(end)
    val steps = ceil(distance / resolution).toInt()

    for (i in 1 until steps) {
      val progress = i.toDouble() / steps
      result.addLast(interpolate(start, end, progress))
    }
  }

  private fun interpolate(pos1: PathPosition, pos2: PathPosition, progress: Double): PathPosition {
    val x = Mth.lerp(progress, pos1.getX(), pos2.getX())
    val y = Mth.lerp(progress, pos1.getY(), pos2.getY())
    val z = Mth.lerp(progress, pos1.getZ(), pos2.getZ())
    return PathPosition(x, y, z)
  }

  private fun buildPath(positions: ArrayDeque<PathPosition>): Path {
    require(positions.isNotEmpty()) { "Cannot build path from empty position list" }

    val path = PathImpl(positions.first(), positions.last(), positions)
    return removeDuplicates(path)
  }

  private fun removeDuplicates(path: Path): Path {
    val EPS = 1e-12
    val result = ArrayDeque<PathPosition>()
    var last: PathPosition? = null

    for (pos in path) {
      if (last == null || !samePoint(last, pos, EPS)) {
        result.addLast(pos)
        last = pos
      }
    }

    return PathImpl(result.first(), result.last(), result)
  }

  private fun samePoint(a: PathPosition, b: PathPosition, eps: Double): Boolean {
    return abs(a.getX() - b.getX()) <= eps &&
      abs(a.getY() - b.getY()) <= eps &&
      abs(a.getZ() - b.getZ()) <= eps
  }

  private fun validateEpsilon(epsilon: Double) {
    require(epsilon > 0.0 && epsilon <= 1.0) { "Epsilon must be in (0.0, 1.0]" }
  }
}
