package org.cobalt.api.pathfinder


import org.cobalt.api.pathfinder.pathing.heuristic.HeuristicContext
import org.cobalt.api.pathfinder.pathing.heuristic.HeuristicWeights
import org.cobalt.api.pathfinder.pathing.heuristic.IHeuristicStrategy
import org.cobalt.api.pathfinder.wrapper.PathPosition

class Node(
  private val position: PathPosition,
  start: PathPosition,
  target: PathPosition,
  heuristicWeights: HeuristicWeights,
  heuristicStrategy: IHeuristicStrategy,
  private val depth: Int,
) : Comparable<Node> {

  private val hCost: Double = heuristicStrategy.calculate(
    HeuristicContext(position, start, target, heuristicWeights)
  )

  private var gCost: Double = 0.0
  private var parent: Node? = null

  fun getPosition(): PathPosition = position

  fun getHeuristic(): Double = hCost

  fun getParent(): Node? = parent

  fun getDepth(): Int = depth

  fun setGCost(gCost: Double) {
    this.gCost = gCost
  }

  fun setParent(parent: Node?) {
    this.parent = parent
  }

  fun isTarget(target: PathPosition): Boolean = this.position == target

  fun getFCost(): Double = getGCost() + getHeuristic()

  fun getGCost(): Double {
    return if (this.parent == null) 0.0 else this.gCost
  }

  override fun equals(other: Any?): Boolean {
    if (other == null || this::class != other::class) return false
    other as Node
    return position == other.position
  }

  override fun hashCode(): Int = position.hashCode()

  override fun compareTo(other: Node): Int {
    val fCostComparison = this.getFCost().compareTo(other.getFCost())
    if (fCostComparison != 0) {
      return fCostComparison
    }

    val heuristicComparison = this.getHeuristic().compareTo(other.getHeuristic())
    if (heuristicComparison != 0) {
      return heuristicComparison
    }

    return this.depth.compareTo(other.depth)
  }
}
