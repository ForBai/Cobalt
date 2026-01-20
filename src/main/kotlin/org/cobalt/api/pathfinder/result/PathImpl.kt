package org.cobalt.api.pathfinder.result

import org.cobalt.api.pathfinder.pathing.result.Path
import org.cobalt.api.pathfinder.wrapper.PathPosition

class PathImpl(
  private val start: PathPosition,
  private val end: PathPosition,
  private val positions: Iterable<PathPosition>,
) : Path {

  private val length: Int = positions.count()

  override fun getStart(): PathPosition = start

  override fun getEnd(): PathPosition = end

  override fun iterator(): Iterator<PathPosition> = positions.iterator()

  override fun length(): Int = length

  override fun collect(): Collection<PathPosition> {
    val collection = ArrayList<PathPosition>(length)
    positions.forEach { collection.add(it) }
    return collection
  }
}
