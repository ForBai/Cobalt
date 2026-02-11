package org.cobalt.api.hud

import org.cobalt.api.module.setting.Setting
import org.cobalt.api.module.setting.SettingsContainer

abstract class HudModule(
  val id: String,
  val name: String,
  val description: String = "",
) : SettingsContainer {

  var enabled: Boolean = true
  var anchor: HudAnchor = HudAnchor.TOP_LEFT
  var offsetX: Float = 10f
  var offsetY: Float = 10f
  var scale: Float = 1.0f

  protected open val defaultAnchor: HudAnchor = HudAnchor.TOP_LEFT
  protected open val defaultOffsetX: Float = 10f
  protected open val defaultOffsetY: Float = 10f
  protected open val defaultScale: Float = 1.0f

  private val settingsList = mutableListOf<Setting<*>>()

  override fun addSetting(vararg settings: Setting<*>) {
    settingsList.addAll(listOf(*settings))
  }

  override fun getSettings(): List<Setting<*>> {
    return settingsList
  }

  abstract fun getBaseWidth(): Float
  abstract fun getBaseHeight(): Float
  abstract fun render(screenX: Float, screenY: Float, scale: Float)

  fun getScaledWidth(): Float = getBaseWidth() * scale
  fun getScaledHeight(): Float = getBaseHeight() * scale

  fun getScreenPosition(screenWidth: Float, screenHeight: Float): Pair<Float, Float> =
    anchor.computeScreenPosition(
      offsetX, offsetY,
      getScaledWidth(), getScaledHeight(),
      screenWidth, screenHeight
    )

  fun resetPosition() {
    anchor = defaultAnchor
    offsetX = defaultOffsetX
    offsetY = defaultOffsetY
    scale = defaultScale
  }

  fun containsPoint(px: Float, py: Float, screenWidth: Float, screenHeight: Float): Boolean {
    val (sx, sy) = getScreenPosition(screenWidth, screenHeight)
    return px >= sx && px <= sx + getScaledWidth() &&
      py >= sy && py <= sy + getScaledHeight()
  }
}
