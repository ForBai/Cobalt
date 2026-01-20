package org.cobalt.api.pathfinder.pathfinder

import java.util.*
import java.util.concurrent.*
import kotlin.math.abs
import kotlin.math.max
import org.cobalt.api.pathfinder.Node
import org.cobalt.api.pathfinder.pathfinder.heap.PrimitiveMinHeap
import org.cobalt.api.pathfinder.pathfinder.processing.EvaluationContextImpl
import org.cobalt.api.pathfinder.pathfinder.processing.SearchContextImpl
import org.cobalt.api.pathfinder.pathing.INeighborStrategy
import org.cobalt.api.pathfinder.pathing.Pathfinder
import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration
import org.cobalt.api.pathfinder.pathing.context.EnvironmentContext
import org.cobalt.api.pathfinder.pathing.hook.PathfinderHook
import org.cobalt.api.pathfinder.pathing.hook.PathfindingContext
import org.cobalt.api.pathfinder.pathing.processing.CostProcessor
import org.cobalt.api.pathfinder.pathing.processing.Processor
import org.cobalt.api.pathfinder.pathing.processing.ValidationProcessor
import org.cobalt.api.pathfinder.pathing.processing.context.SearchContext
import org.cobalt.api.pathfinder.pathing.result.Path
import org.cobalt.api.pathfinder.pathing.result.PathState
import org.cobalt.api.pathfinder.pathing.result.PathfinderResult
import org.cobalt.api.pathfinder.provider.NavigationPointProvider
import org.cobalt.api.pathfinder.result.PathImpl
import org.cobalt.api.pathfinder.result.PathfinderResultImpl
import org.cobalt.api.pathfinder.wrapper.Depth
import org.cobalt.api.pathfinder.wrapper.PathPosition

abstract class AbstractPathfinder(
  protected val pathfinderConfiguration: PathfinderConfiguration,
) : Pathfinder {

  companion object {
    private val EMPTY_PATH_POSITIONS: Set<PathPosition> = LinkedHashSet<PathPosition>(0)
    private const val TIE_BREAKER_WEIGHT = 1e-6

    private val PATHING_EXECUTOR_SERVICE: ExecutorService =
      Executors.newWorkStealingPool(max(1, Runtime.getRuntime().availableProcessors() / 2))

    init {
      Runtime.getRuntime().addShutdownHook(Thread { shutdownExecutor() })
    }

    private fun shutdownExecutor() {
      PATHING_EXECUTOR_SERVICE.shutdown()
      try {
        if (!PATHING_EXECUTOR_SERVICE.awaitTermination(5, TimeUnit.SECONDS)) {
          PATHING_EXECUTOR_SERVICE.shutdownNow()
        }
      } catch (e: InterruptedException) {
        PATHING_EXECUTOR_SERVICE.shutdownNow()
        Thread.currentThread().interrupt()
      }
    }
  }

  protected val navigationPointProvider: NavigationPointProvider = pathfinderConfiguration.provider
  protected val validationProcessors: List<ValidationProcessor>? = pathfinderConfiguration.getNodeValidationProcessors()
  protected val costProcessors: List<CostProcessor>? = pathfinderConfiguration.getNodeCostProcessors()
  protected val neighborStrategy: INeighborStrategy = pathfinderConfiguration.neighborStrategy

  private val pathfinderHooks: MutableSet<PathfinderHook> = Collections.synchronizedSet(HashSet())

  @Volatile
  private var abortRequested = false

  override fun findPath(
    start: PathPosition,
    target: PathPosition,
    context: EnvironmentContext?,
  ): CompletionStage<PathfinderResult> {
    this.abortRequested = false
    return initiatePathing(start, target, context)
  }

  override fun abort() {
    this.abortRequested = true
  }

  override fun registerPathfindingHook(hook: PathfinderHook) {
    this.pathfinderHooks.add(hook)
  }

  private fun initiatePathing(
    start: PathPosition,
    target: PathPosition,
    environmentContext: EnvironmentContext?,
  ): CompletionStage<PathfinderResult> {
    val effectiveStart = start.floor()
    val effectiveTarget = target.floor()

    return if (pathfinderConfiguration.async) {
      CompletableFuture.supplyAsync(
        { executePathingAlgorithm(effectiveStart, effectiveTarget, environmentContext) },
        PATHING_EXECUTOR_SERVICE
      ).exceptionally { throwable -> handlePathingException(start, target, throwable) }
    } else {
      try {
        CompletableFuture.completedFuture(executePathingAlgorithm(effectiveStart, effectiveTarget, environmentContext))
      } catch (e: Exception) {
        CompletableFuture.completedFuture(handlePathingException(start, target, e))
      }
    }
  }

  private fun executePathingAlgorithm(
    start: PathPosition,
    target: PathPosition,
    environmentContext: EnvironmentContext?,
  ): PathfinderResult {
    initializeSearch()

    val searchContext = SearchContextImpl(
      start,
      target,
      this.pathfinderConfiguration,
      this.navigationPointProvider,
      environmentContext
    )

    val processors = getProcessors()

    try {
      processors.forEach { it.initializeSearch(searchContext) }

      val startNode = createStartNode(start, target)
      val startNodeContext = EvaluationContextImpl(
        searchContext,
        startNode,
        null,
        pathfinderConfiguration.heuristicStrategy
      )

      if (!validationProcessors.isNullOrEmpty()) {
        val isStartNodeInvalid = validationProcessors.any { !it.isValid(startNodeContext) }
        if (isStartNodeInvalid) {
          return PathfinderResultImpl(PathState.FAILED, PathImpl(start, target, EMPTY_PATH_POSITIONS))
        }
      }

      val openSet = PrimitiveMinHeap(1024)
      val startKey = try {
        calculateHeapKey(startNode, startNode.getFCost())
      } catch (t: Throwable) {
        startNode.getFCost()
      }

      insertStartNode(startNode, startKey, openSet)

      var currentDepth = 0
      var bestFallbackNode = startNode

      while (!openSet.isEmpty() && currentDepth < pathfinderConfiguration.maxIterations) {
        currentDepth++

        if (this.abortRequested) {
          return createAbortedResult(start, target, bestFallbackNode)
        }

        val currentNode = extractBestNode(openSet)
        markNodeAsExpanded(currentNode)

        pathfinderHooks.forEach { hook ->
          hook.onPathfindingStep(PathfindingContext(currentNode.getPosition(), Depth.of(currentDepth)))
        }

        if (currentNode.getHeuristic() < bestFallbackNode.getHeuristic()) {
          bestFallbackNode = currentNode
        }

        if (hasReachedPathLengthLimit(currentNode)) {
          return PathfinderResultImpl(PathState.LENGTH_LIMITED, reconstructPath(start, target, currentNode))
        }

        if (currentNode.isTarget(target)) {
          return PathfinderResultImpl(PathState.FOUND, reconstructPath(start, target, currentNode))
        }

        processSuccessors(start, target, currentNode, openSet, searchContext)
      }

      return determinePostLoopResult(currentDepth, start, target, bestFallbackNode)

    } catch (e: Exception) {
      return PathfinderResultImpl(PathState.FAILED, PathImpl(start, target, EMPTY_PATH_POSITIONS))
    } finally {
      val finalizeErrors = mutableListOf<Throwable>()
      processors.forEach { processor ->
        try {
          processor.finalizeSearch(searchContext)
        } catch (e: Exception) {
          finalizeErrors.add(e)
        }
      }
      performAlgorithmCleanup()
    }
  }

  fun calculateHeapKey(neighbor: Node, fCost: Double): Double {
    val heuristic = neighbor.getHeuristic()
    val tieBreaker = TIE_BREAKER_WEIGHT * (heuristic / (abs(fCost) + 1))
    var heapKey = fCost - tieBreaker

    if (heapKey.isNaN() || heapKey.isInfinite()) {
      heapKey = fCost
    }

    return heapKey
  }

  private fun getProcessors(): List<Processor> {
    val processors = mutableListOf<Processor>()

    validationProcessors?.let { processors.addAll(it) }
    costProcessors?.let { processors.addAll(it) }

    return processors
  }

  private fun createAbortedResult(start: PathPosition, target: PathPosition, fallbackNode: Node): PathfinderResult {
    this.abortRequested = false
    return PathfinderResultImpl(PathState.ABORTED, reconstructPath(start, target, fallbackNode))
  }

  private fun handlePathingException(
    originalStart: PathPosition,
    originalTarget: PathPosition,
    throwable: Throwable,
  ): PathfinderResult {
    return PathfinderResultImpl(PathState.FAILED, PathImpl(originalStart, originalTarget, EMPTY_PATH_POSITIONS))
  }

  protected fun createStartNode(startPos: PathPosition, targetPos: PathPosition): Node {
    return Node(
      startPos,
      startPos,
      targetPos,
      pathfinderConfiguration.heuristicWeights,
      pathfinderConfiguration.heuristicStrategy,
      0
    )
  }

  private fun hasReachedPathLengthLimit(currentNode: Node): Boolean {
    val maxLength = pathfinderConfiguration.maxLength
    return maxLength > 0 && currentNode.getDepth() >= maxLength
  }

  private fun determinePostLoopResult(
    depthReached: Int,
    start: PathPosition,
    target: PathPosition,
    fallbackNode: Node,
  ): PathfinderResult {
    return when {
      depthReached >= pathfinderConfiguration.maxIterations -> {
        PathfinderResultImpl(PathState.MAX_ITERATIONS_REACHED, reconstructPath(start, target, fallbackNode))
      }

      pathfinderConfiguration.fallback -> {
        PathfinderResultImpl(PathState.FALLBACK, reconstructPath(start, target, fallbackNode))
      }

      else -> {
        PathfinderResultImpl(PathState.FAILED, PathImpl(start, target, EMPTY_PATH_POSITIONS))
      }
    }
  }

  protected fun reconstructPath(start: PathPosition, target: PathPosition, endNode: Node): Path {
    if (endNode.getParent() == null && endNode.getDepth() == 0) {
      return PathImpl(start, target, listOf(endNode.getPosition()))
    }

    val pathPositions = tracePathPositionsFromNode(endNode)
    return PathImpl(start, target, pathPositions)
  }

  private fun tracePathPositionsFromNode(leafNode: Node): List<PathPosition> {
    val path = mutableListOf<PathPosition>()
    var currentNode: Node? = leafNode

    while (currentNode != null) {
      path.add(currentNode.getPosition())
      currentNode = currentNode.getParent()
    }

    return path.reversed()
  }

  protected abstract fun insertStartNode(node: Node, fCost: Double, openSet: PrimitiveMinHeap)

  protected abstract fun extractBestNode(openSet: PrimitiveMinHeap): Node

  protected abstract fun initializeSearch()

  protected abstract fun markNodeAsExpanded(node: Node)

  protected abstract fun performAlgorithmCleanup()

  protected abstract fun processSuccessors(
    requestStart: PathPosition,
    requestTarget: PathPosition,
    currentNode: Node,
    openSet: PrimitiveMinHeap,
    searchContext: SearchContext,
  )
}
