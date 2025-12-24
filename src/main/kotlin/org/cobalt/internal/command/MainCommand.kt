package org.cobalt.internal.command

import java.awt.Color
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.cobalt.api.command.Command
import org.cobalt.api.command.annotation.DefaultHandler
import org.cobalt.api.command.annotation.SubCommand
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.WorldRenderEvent
import org.cobalt.api.feat.pathfinder.PathfindingBundle
import org.cobalt.api.util.ChatUtils
import org.cobalt.api.util.render.Render3D
import org.cobalt.internal.feat.general.NameProtect
import org.cobalt.internal.ui.screen.UIScreen

object MainCommand : Command(
  name = "cobalt",
  aliases = arrayOf("cb")
) {

  private var path: List<BlockPos>? = null

  @DefaultHandler
  fun main() {
    UIScreen.openUI()
  }

  @SubCommand
  fun dev(subCmd: String) {
    when (subCmd) {
      "tnp" -> NameProtect.isEnabled = !NameProtect.isEnabled
    }
  }

  @SubCommand
  fun pathfind(x: Int, y: Int, z: Int) {
    val curr = System.currentTimeMillis()
    path = PathfindingBundle.pathFinding.findPath(BlockPos(x, y, z), MinecraftClient.getInstance().player!!)
  }

  @SubscribeEvent
  fun onRender(event: WorldRenderEvent.Last) {
    val currentPath = path ?: return
    var prev: Vec3d? = null

    for (pos in currentPath) {
      val center = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)

      prev?.let { vec ->
        Render3D.drawLine(event.context, vec, center, color = Color(94, 115, 255), esp = true, thickness = 1F)
      }

      val box = Box(
        pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
        pos.x + 1.0, pos.y - 1.0, pos.z + 1.0
      )

      Render3D.drawBox(event.context, box, esp = true, color = Color(94, 115, 255))
      prev = center
    }
  }

}
