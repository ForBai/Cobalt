package org.cobalt.internal.ui.hud

import org.cobalt.api.hud.HudModule
import org.cobalt.api.ui.theme.ThemeManager
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.animation.ColorAnimation
import org.cobalt.internal.ui.util.isHoveringOver

internal class HudSettingsPopup {
  var visible: Boolean = false
  var module: HudModule? = null
  var popupX: Float = 0f
  var popupY: Float = 0f

  private val popupWidth = 180f
  private val popupPadding = 12f
  private val rowHeight = 28f
  private val headerHeight = 26f
  private val toggleAnim = ColorAnimation(150L)
  private val buttonAnim = ColorAnimation(150L)

  private val popupHeight: Float
    get() = popupPadding * 2f + headerHeight + rowHeight + rowHeight + 8f

  fun show(module: HudModule, x: Float, y: Float) {
    this.module = module
    popupX = x
    popupY = y
    visible = true
  }

  fun hide() {
    visible = false
    module = null
  }

  fun render() {
    if (!visible) return
    val target = module ?: return

    NVGRenderer.rect(popupX, popupY, popupWidth, popupHeight, ThemeManager.currentTheme.panel, 8f)
    NVGRenderer.hollowRect(popupX, popupY, popupWidth, popupHeight, 1f, ThemeManager.currentTheme.controlBorder, 8f)

    NVGRenderer.text(
      target.name,
      popupX + popupPadding,
      popupY + popupPadding + 2f,
      14f,
      ThemeManager.currentTheme.accent
    )

    val toggleY = popupY + popupPadding + headerHeight
    val toggleText = if (target.enabled) "Disable" else "Enable"
    val toggleWidth = NVGRenderer.textWidth(toggleText, 12f) + 30f
    val toggleX = popupX + popupWidth - toggleWidth - popupPadding
    val isToggleHover = isHoveringOver(toggleX, toggleY, toggleWidth, rowHeight - 6f)
    val toggleBg = toggleAnim.get(
      ThemeManager.currentTheme.controlBg,
      ThemeManager.currentTheme.selectedOverlay,
      !isToggleHover
    )
    val toggleBorder = toggleAnim.get(
      ThemeManager.currentTheme.controlBorder,
      ThemeManager.currentTheme.accent,
      !isToggleHover
    )

    NVGRenderer.text(
      "Enabled",
      popupX + popupPadding,
      toggleY + 6f,
      12f,
      ThemeManager.currentTheme.textSecondary
    )
    NVGRenderer.rect(toggleX, toggleY, toggleWidth, rowHeight - 6f, toggleBg, 10f)
    NVGRenderer.hollowRect(toggleX, toggleY, toggleWidth, rowHeight - 6f, 1.5f, toggleBorder, 10f)
    NVGRenderer.text(
      toggleText,
      toggleX + 12f,
      toggleY + 5f,
      12f,
      ThemeManager.currentTheme.textPrimary
    )

    val resetY = toggleY + rowHeight + 6f
    val resetHover = isHoveringOver(popupX + popupPadding, resetY, popupWidth - popupPadding * 2f, rowHeight - 6f)
    val resetBg = buttonAnim.get(
      ThemeManager.currentTheme.controlBg,
      ThemeManager.currentTheme.selectedOverlay,
      !resetHover
    )
    val resetBorder = buttonAnim.get(
      ThemeManager.currentTheme.controlBorder,
      ThemeManager.currentTheme.accent,
      !resetHover
    )

    NVGRenderer.rect(
      popupX + popupPadding,
      resetY,
      popupWidth - popupPadding * 2f,
      rowHeight - 6f,
      resetBg,
      8f
    )
    NVGRenderer.hollowRect(
      popupX + popupPadding,
      resetY,
      popupWidth - popupPadding * 2f,
      rowHeight - 6f,
      1.5f,
      resetBorder,
      8f
    )
    NVGRenderer.text(
      "Reset Position",
      popupX + popupPadding + 10f,
      resetY + 5f,
      12f,
      ThemeManager.currentTheme.textPrimary
    )
  }

  fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
    if (!visible) return false
    val target = module ?: return false
    if (button != 0) return false

    val toggleY = popupY + popupPadding + headerHeight
    val toggleText = if (target.enabled) "Disable" else "Enable"
    val toggleWidth = NVGRenderer.textWidth(toggleText, 12f) + 30f
    val toggleX = popupX + popupWidth - toggleWidth - popupPadding

    if (mouseX >= toggleX && mouseX <= toggleX + toggleWidth && mouseY >= toggleY && mouseY <= toggleY + rowHeight - 6f) {
      target.enabled = !target.enabled
      toggleAnim.start()
      return true
    }

    val resetY = toggleY + rowHeight + 6f
    if (mouseX >= popupX + popupPadding && mouseX <= popupX + popupWidth - popupPadding && mouseY >= resetY && mouseY <= resetY + rowHeight - 6f) {
      target.resetPosition()
      buttonAnim.start()
      return true
    }

    return containsPoint(mouseX, mouseY)
  }

  fun containsPoint(px: Float, py: Float): Boolean {
    if (!visible) return false
    return px >= popupX && px <= popupX + popupWidth && py >= popupY && py <= popupY + popupHeight
  }
}
