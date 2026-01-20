package org.cobalt.api.pathfinder.pathfinder

import it.unimi.dsi.fastutil.longs.Long2DoubleMap
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlin.math.abs
import kotlin.math.max
import net.minecraft.util.Mth
import org.cobalt.api.pathfinder.Node
import org.cobalt.api.pathfinder.pathfinder.heap.PrimitiveMinHeap
import org.cobalt.api.pathfinder.pathfinder.processing.EvaluationContextImpl
import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration
import org.cobalt.api.pathfinder.pathing.processing.Cost
import org.cobalt.api.pathfinder.pathing.processing.context.EvaluationContext
import org.cobalt.api.pathfinder.pathing.processing.context.SearchContext
import org.cobalt.api.pathfinder.util.GridRegionData
import org.cobalt.api.pathfinder.util.RegionKey
import org.cobalt.api.pathfinder.wrapper.PathPosition

class AStarPathfinder(configuration: PathfinderConfiguration) : AbstractPathfinder(configuration) {

  private val currentSession = ThreadLocal<PathfindingSession>()

  override fun insertStartNode(node: Node, fCost: Double, openSet: PrimitiveMinHeap) {
    val session = getSessionOrThrow()
    val packedPos = RegionKey.pack(node.getPosition())
    openSet.insertOrUpdate(packedPos, fCost)
    session.openSetNodes[packedPos] = node
  }

  override fun extractBestNode(openSet: PrimitiveMinHeap): Node {
    val session = getSessionOrThrow()
    val packedPos = openSet.extractMin()
    val node = session.openSetNodes[packedPos]!!
    session.openSetNodes.remove(packedPos)
    return node
  }

  override fun initializeSearch() {
    currentSession.set(PathfindingSession())
  }

  override fun processSuccessors(
    start: PathPosition,
    target: PathPosition,
    currentNode: Node,
    openSet: PrimitiveMinHeap,
    searchContext: SearchContext,
  ) {
    val session = getSessionOrThrow()
    val offsets = neighborStrategy.getOffsets(currentNode.getPosition())

    for (offset in offsets) {
      val neighborPos = currentNode.getPosition().add(offset)
      val packedPos = RegionKey.pack(neighborPos)

      if (openSet.contains(packedPos)) {
        val existing = session.openSetNodes[packedPos]!!
        updateExistingNode(existing, packedPos, currentNode, searchContext, openSet)
        continue
      }

      val regionData = session.getOrCreateRegionData(neighborPos)
      if (regionData.getBloomFilter().mightContain(neighborPos) &&
        regionData.getRegionalExaminedPositions().contains(packedPos)
      ) {

        var shouldReopen = false
        if (pathfinderConfiguration.shouldReopenClosedNodes()) {
          val oldCost = session.closedSetGCosts[packedPos]

          val tempNeighbor = createNeighborNode(neighborPos, start, target, currentNode)
          val context = EvaluationContextImpl(
            searchContext,
            tempNeighbor,
            currentNode,
            pathfinderConfiguration.heuristicStrategy
          )
          val newGCost = calculateGCost(context)

          if (oldCost.isNaN() || newGCost + Math.ulp(newGCost) < oldCost) {
            session.closedSetGCosts[packedPos] = newGCost
            shouldReopen = true
          }
        }

        if (!shouldReopen) continue
      }

      val neighbor = createNeighborNode(neighborPos, start, target, currentNode)
      neighbor.setParent(currentNode)

      val context = EvaluationContextImpl(
        searchContext,
        neighbor,
        currentNode,
        pathfinderConfiguration.heuristicStrategy
      )

      if (!isValidByCustomProcessors(context)) {
        continue
      }

      val gCost = calculateGCost(context)
      neighbor.setGCost(gCost)
      val fCost = neighbor.getFCost()
      val heapKey = calculateHeapKey(neighbor, fCost)

      openSet.insertOrUpdate(packedPos, heapKey)
      session.openSetNodes[packedPos] = neighbor
    }
  }

  private fun updateExistingNode(
    existing: Node,
    packedPos: Long,
    currentNode: Node,
    searchContext: SearchContext,
    openSet: PrimitiveMinHeap,
  ) {
    val context = EvaluationContextImpl(
      searchContext,
      existing,
      currentNode,
      pathfinderConfiguration.heuristicStrategy
    )

    val newG = calculateGCost(context)
    val tol = Math.ulp(max(abs(newG), abs(existing.getGCost())))

    if (newG + tol >= existing.getGCost()) return

    if (!isValidByCustomProcessors(context)) {
      return
    }

    existing.setParent(currentNode)
    existing.setGCost(newG)
    val newF = existing.getFCost()
    val newKey = calculateHeapKey(existing, newF)
    val oldKey = openSet.getCost(packedPos)

    if (newKey + Math.ulp(newKey) < oldKey) {
      openSet.insertOrUpdate(packedPos, newKey)
    } else if (abs(newKey - oldKey) <= Math.ulp(newKey)) {
      openSet.insertOrUpdate(packedPos, oldKey - Math.ulp(oldKey))
    }
  }

  private fun createNeighborNode(
    position: PathPosition,
    start: PathPosition,
    target: PathPosition,
    parent: Node,
  ): Node {
    return Node(
      position,
      start,
      target,
      pathfinderConfiguration.heuristicWeights,
      pathfinderConfiguration.heuristicStrategy,
      parent.getDepth() + 1
    )
  }

  private fun isValidByCustomProcessors(context: EvaluationContext): Boolean {
    if (validationProcessors.isNullOrEmpty()) {
      return true
    }

    for (validator in validationProcessors) {
      if (!validator.isValid(context)) {
        return false
      }
    }
    return true
  }

  private fun calculateGCost(context: EvaluationContext): Double {
    val baseCost = context.getBaseTransitionCost()
    var additionalCost = 0.0

    if (!costProcessors.isNullOrEmpty()) {
      for (processor in costProcessors) {
        val contribution = processor.calculateCostContribution(context)
        additionalCost += contribution?.value ?: Cost.ZERO.value
      }
    }

    var transitionCost = baseCost + additionalCost
    if (transitionCost < 0) {
      transitionCost = 0.0
    }

    return context.getPathCostToPreviousPosition() + transitionCost
  }

  override fun markNodeAsExpanded(node: Node) {
    val session = getSessionOrThrow()
    val position = node.getPosition()
    val packedPos = RegionKey.pack(position)

    session.openSetNodes.remove(packedPos)

    if (pathfinderConfiguration.shouldReopenClosedNodes()) {
      session.closedSetGCosts[packedPos] = node.getGCost()
    }

    val regionData = session.getOrCreateRegionData(position)
    regionData.getBloomFilter().put(position)
    regionData.getRegionalExaminedPositions().add(packedPos)
  }

  override fun performAlgorithmCleanup() {
    currentSession.remove()
  }

  private fun getSessionOrThrow(): PathfindingSession {
    return currentSession.get()
      ?: throw IllegalStateException("Pathfinding session not initialized. Call initializeSearch() first.")
  }

  private inner class PathfindingSession {
    val visitedRegions: Long2ObjectMap<GridRegionData> = Long2ObjectOpenHashMap()
    val openSetNodes: Long2ObjectMap<Node> = Long2ObjectOpenHashMap()
    val closedSetGCosts: Long2DoubleMap = Long2DoubleOpenHashMap().apply {
      defaultReturnValue(Double.NaN)
    }

    fun getOrCreateRegionData(position: PathPosition): GridRegionData {
      val cellSize = pathfinderConfiguration.gridCellSize
      val rX = Mth.floorDiv(position.getFlooredX(), cellSize)
      val rY = Mth.floorDiv(position.getFlooredY(), cellSize)
      val rZ = Mth.floorDiv(position.getFlooredZ(), cellSize)
      val regionKey = RegionKey.pack(rX, rY, rZ)

      return visitedRegions.computeIfAbsent(regionKey) {
        GridRegionData(pathfinderConfiguration)
      }
    }
  }
}
