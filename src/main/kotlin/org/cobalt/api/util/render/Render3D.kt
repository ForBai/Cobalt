package org.cobalt.api.util.render

import java.awt.Color
import net.minecraft.gizmos.GizmoStyle
import net.minecraft.gizmos.Gizmos
import net.minecraft.util.ARGB
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.cobalt.api.event.impl.render.WorldRenderContext

object Render3D {

  @JvmStatic
  fun drawBox(context: WorldRenderContext, box: AABB, color: Color, esp: Boolean = false) {
    if (!FrustumUtils.isVisible(context.frustum, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
      return
    }

    val strokeColor = ARGB.color(color.alpha, color.red, color.green, color.blue)
    val fillColor = ARGB.color(150, color.red, color.green, color.blue)

    val style = GizmoStyle.strokeAndFill(strokeColor, 2.5f, fillColor)
    val props = Gizmos.cuboid(box, style)

    if (esp) {
      props.setAlwaysOnTop()
    }
  }

  @JvmStatic
  fun drawLine(
    context: WorldRenderContext,
    start: Vec3,
    end: Vec3,
    color: Color,
    esp: Boolean = false,
    thickness: Float = 1f,
  ) {
    if (!FrustumUtils.isVisible(
        context.frustum,
        minOf(start.x, end.x), minOf(start.y, end.y), minOf(start.z, end.z),
        maxOf(start.x, end.x), maxOf(start.y, end.y), maxOf(start.z, end.z)
      )
    ) return

    val argbColor = ARGB.color(color.alpha, color.red, color.green, color.blue)
    val props = Gizmos.line(start, end, argbColor, thickness)

    if (esp) {
      props.setAlwaysOnTop()
    }
  }

}
