package org.cobalt.api.util

import kotlin.math.atan2
import kotlin.math.sqrt
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import org.cobalt.api.util.helper.Rotation

object AngleUtils {

  private val mc: MinecraftClient =
    MinecraftClient.getInstance()

  private val player: ClientPlayerEntity?
    get() = mc.player

  fun normalizeYaw(angle: Float): Float {
    var a = angle % 360f
    if (a >= 180f) a -= 360f
    if (a < -180f) a += 360f
    return a
  }

  fun getRotation(from: Vec3d, to: Vec3d): Rotation {
    val xDiff = to.x - from.x
    val yDiff = to.y - from.y
    val zDiff = to.z - from.z

    val dist =
      sqrt(xDiff * xDiff + zDiff * zDiff)

    return Rotation(Math.toDegrees(atan2(zDiff, xDiff)).toFloat() - 90f, -Math.toDegrees(atan2(yDiff, dist)).toFloat())
  }

  fun getRotation(to: Vec3d): Rotation {
    return getRotation(player!!.eyePos, to)
  }

  fun getRotation(to: Entity): Rotation {
    return getRotation(
      player!!.eyePos,
      to.entityPos.add(0.0, ((to.height * 0.85) + (Math.random() * 0.3 - 0.15)).coerceAtMost(1.7), 0.0)
    )
  }

}
