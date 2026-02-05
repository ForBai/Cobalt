package org.cobalt.api.pathfinder

import java.awt.Color
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.client.TickEvent
import org.cobalt.api.event.impl.render.WorldRenderEvent
import org.cobalt.api.pathfinder.factory.impl.AStarPathfinderFactory
import org.cobalt.api.pathfinder.pathing.NeighborStrategies
import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration
import org.cobalt.api.pathfinder.pathing.processing.impl.MinecraftPathProcessor
import org.cobalt.api.pathfinder.pathing.result.Path
import org.cobalt.api.pathfinder.pathing.result.PathState
import org.cobalt.api.pathfinder.provider.impl.MinecraftNavigationProvider
import org.cobalt.api.pathfinder.wrapper.PathPosition
import org.cobalt.api.util.ChatUtils
import org.cobalt.api.util.render.Render3D

/*
 * TODO: im lazy right now, but chunk and world caching would be alot better,
 *  if someone could help me do this it would be a great help :))
 */
object PathExecutor {

  private val mc: Minecraft = Minecraft.getInstance()
  private var currentPath: Path? = null
  private var currentWaypointIndex: Int = 0

  init {
    EventBus.register(this)
  }

  fun start(x: Double, y: Double, z: Double) {
    val player = mc.player ?: return
    val start = PathPosition(player.x, player.y, player.z)
    val target = PathPosition(x, y, z)

    val factory = AStarPathfinderFactory()

    val processor = MinecraftPathProcessor()
    val config =
      PathfinderConfiguration.builder()
        .provider(MinecraftNavigationProvider())
        .maxIterations(20000)
        .async(true)
        .neighborStrategy(NeighborStrategies.HORIZONTAL_DIAGONAL_AND_VERTICAL)
        .nodeValidationProcessors(listOf(processor))
        .nodeCostProcessors(listOf(processor))
        .build()

    val pathfinder = factory.createPathfinder(config)

    ChatUtils.sendDebug("Calculating path to $x, $y, $z...")
    val startTime = System.currentTimeMillis()
    pathfinder.findPath(start, target).thenAccept { result ->
      val duration = System.currentTimeMillis() - startTime
      if (result.successful()) {
        currentPath = result.getPath()
        currentWaypointIndex = 0

        val state = result.getPathState()
        val path = result.getPath()

        if (state == PathState.FOUND) {
          ChatUtils.sendMessage(
            "§aPath found! §7Calculated in §f${duration}ms §8(${path.length()} nodes)"
          )
        } else {
          /*
           * partial paths can happen, i would recommend improving this functionality
           * to whoever maintainer wants to maintain my horrible shitcode of a pathfinder.
           * i think partial paths should be refactored, i will write all the cases now
           * iteration limit -> self explanatory xd (or if u cant use ur brain
           * then its just if the algorithm takes too many iterations to find a goal.
           * this is set literaaally like 20 lines above you...)
           * fallback -> pf searches everywhere and couldnt find a possible way to get there.
           * this can happen in the case of unloaded chunks, or an obstruction
           */
          ChatUtils.sendMessage(
            "§ePartial path found! §7Calculated in §f${duration}ms §8(${path.length()} nodes)"
          )
        }
      } else {
        ChatUtils.sendMessage("§cFailed to find path: ${result.getPathState()}")
      }
    }
  }

  fun stop() {
    currentPath = null
    currentWaypointIndex = 0
  }

  /*
   * as of writing this, there is intentionally no moving, rotations,
   * or jumping yet. i ask that you make sure that the algo works good
   * before implementing them, aswell as rots
   */
  @SubscribeEvent
  fun onTick(@Suppress("UNUSED_PARAMETER") event: TickEvent.Start) {
    val path = currentPath ?: return
    val player = mc.player ?: return

    val waypoints = path.collect().toList()
    if (currentWaypointIndex >= waypoints.size) {
      ChatUtils.sendMessage("Reached the end!") // this is kinda icky, someone change this lol
      stop()
      return
    }

    val targetPos = waypoints[currentWaypointIndex].mid()
    val targetVec = Vec3(targetPos.x, targetPos.y, targetPos.z)

    val horizontalDistSq =
      (player.x - targetVec.x) * (player.x - targetVec.x) +
        (player.z - targetVec.z) * (player.z - targetVec.z)
    if (horizontalDistSq < 0.25) {
      currentWaypointIndex++
    }
  }

  @SubscribeEvent
  fun onRender(event: WorldRenderEvent.Last) {
    val path = currentPath ?: return
    val waypoints = path.collect().toList()

    if (waypoints.size < 2) return

    for (i in 0 until waypoints.size - 1) {
      val start = waypoints[i].mid()
      val end = waypoints[i + 1].mid()

      Render3D.drawLine(
        event.context,
        Vec3(start.x, start.y, start.z),
        Vec3(end.x, end.y, end.z),
        Color.CYAN,
        true,
        2.0f
      )
    }

    if (currentWaypointIndex < waypoints.size) {
      val currentPos = waypoints[currentWaypointIndex].mid()
      Render3D.drawBox(
        event.context,
        AABB(
          currentPos.x - 0.25,
          currentPos.y - 0.25,
          currentPos.z - 0.25,
          currentPos.x + 0.25,
          currentPos.y + 0.25,
          currentPos.z + 0.25
        ),
        Color.GREEN,
        true
      )
    }
  }
}
