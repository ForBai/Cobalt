package org.cobalt.api.rotation

import kotlin.math.roundToInt
import net.minecraft.client.MinecraftClient
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.WorldRenderEvent
import org.cobalt.api.util.AngleUtils
import org.cobalt.api.util.PlayerUtils
import org.cobalt.api.util.helper.Rotation
import org.cobalt.api.util.player.MovementManager

object RotationExecutor {

  private val mc: MinecraftClient =
    MinecraftClient.getInstance()

  private var targetYaw: Float = 0F
  private var targetPitch: Float = 0F

  private var currStrat: IRotationStrategy? = null
  private var isRotating: Boolean = false

  fun rotateTo(
    endRot: Rotation,
    strategy: IRotationStrategy,
  ) {
    stopRotating()

    targetYaw = endRot.yaw
    targetPitch = endRot.pitch
    currStrat = strategy

    strategy.onStart()
    isRotating = true
  }

  fun stopRotating() {
    currStrat?.onStop()
    currStrat = null
    isRotating = false
  }

  fun isRotating(): Boolean {
    return isRotating
  }

  @SubscribeEvent
  fun onRotate(
    event: WorldRenderEvent.Last,
  ) {
    val player = mc.player ?: return

    if (!isRotating) {
      return
    }

    currStrat?.let {
      val result = it.onRotate(
        player,
        targetYaw,
        targetPitch
      )

      if (result == null) {
        player.yaw = applyGCD(targetYaw, player.yaw)
        player.pitch = applyGCD(targetPitch, player.pitch)
        stopRotating()
      } else {
        player.yaw = applyGCD(result.yaw, player.yaw)
        player.pitch = applyGCD(result.pitch, player.pitch)
      }
    }
  }

  /**
   * Applies the mouse sensitivity GCD fix to rotations to prevent anti-cheat flags. Credit to oblongboot for this!
   *
   * @param rotation The target rotation.
   * @param prevRotation The previous rotation.
   * @param min Optional minimum bound.
   * @param max Optional maximum bound.
   * @return The adjusted rotation value.
   */
  private fun applyGCD(rotation: Float, prevRotation: Float, min: Float? = null, max: Float? = null): Float {
    val sensitivity = mc.options.mouseSensitivity.value
    val f = sensitivity * 0.6 + 0.2
    val gcd = f * f * f * 1.2

    val delta = getRotationDelta(prevRotation, rotation)
    val roundedDelta = (delta / gcd).roundToInt() * gcd
    var result = prevRotation + roundedDelta

    if (max != null && result > max) {
      result -= gcd
    }
    if (min != null && result < min) {
      result += gcd
    }

    return result.toFloat()
  }

  private fun getRotationDelta(from: Float, to: Float): Float {
    var delta = normalizeAngle(to) - normalizeAngle(from)
    if (delta > 180f) delta -= 360f
    if (delta < -180f) delta += 360f
    return delta
  }

  private fun normalizeAngle(angle: Float): Float {
    var result = angle
    while (result > 180f) result -= 360f
    while (result < -180f) result += 360f
    return result
  }

}
