package org.cobalt.api.hud

import org.cobalt.api.module.Module
import org.cobalt.api.module.setting.Setting
import org.cobalt.api.module.setting.SettingsContainer

/**
 * DSL extension to create a HUD element inside a Module.
 *
 * Example:
 * ```
 * class MyModule : Module("My Module") {
 *   val speedHud = hudElement("speed", "Speed Display") {
 *     width { 80f }
 *     height { 20f }
 *     anchor = HudAnchor.TOP_RIGHT
 *     val showDecimals = setting(CheckboxSetting("Decimals", "", false))
 *     render { x, y, scale -> /* use showDecimals.value */ }
 *   }
 * }
 * ```
 */
fun Module.hudElement(
  id: String,
  name: String,
  description: String = "",
  init: HudElementBuilder.() -> Unit
): HudElement {
  val builder = HudElementBuilder(id, name, description)
  builder.init()
  val element = builder.build()
  addHudElement(element)
  return element
}

class HudElementBuilder(
  private val id: String,
  private val name: String,
  private val description: String = ""
) : SettingsContainer {

  private var widthProvider: () -> Float = { 100f }
  private var heightProvider: () -> Float = { 20f }
  var anchor: HudAnchor = HudAnchor.TOP_LEFT
  var offsetX: Float = 10f
  var offsetY: Float = 10f
  var scale: Float = 1.0f
  private var renderLambda: ((Float, Float, Float) -> Unit)? = null

  private val settingsList = mutableListOf<Setting<*>>()

  override fun addSetting(vararg settings: Setting<*>) {
    settingsList.addAll(listOf(*settings))
  }

  override fun getSettings(): List<Setting<*>> {
    return settingsList
  }

  fun width(provider: () -> Float) {
    widthProvider = provider
  }

  fun height(provider: () -> Float) {
    heightProvider = provider
  }

  fun <T, S : Setting<T>> setting(setting: S): S {
    addSetting(setting)
    return setting
  }

  fun render(block: (screenX: Float, screenY: Float, scale: Float) -> Unit) {
    renderLambda = block
  }

  fun build(): HudElement {
    val capturedRender = renderLambda ?: { _, _, _ -> }
    val capturedWidth = widthProvider
    val capturedHeight = heightProvider
    val capturedSettings = settingsList.toList()
    val capturedAnchor = anchor
    val capturedOffsetX = offsetX
    val capturedOffsetY = offsetY
    val capturedScale = scale

    return object : HudElement(id, name, description) {
      override val defaultAnchor = capturedAnchor
      override val defaultOffsetX = capturedOffsetX
      override val defaultOffsetY = capturedOffsetY
      override val defaultScale = capturedScale

      init {
        capturedSettings.forEach { addSetting(it) }
        resetPosition()
      }

      override fun getBaseWidth(): Float = capturedWidth()
      override fun getBaseHeight(): Float = capturedHeight()
      override fun render(screenX: Float, screenY: Float, scale: Float) {
        capturedRender(screenX, screenY, scale)
      }
    }
  }
}
