package org.cobalt.internal.ui.screen

import java.awt.Color
import net.minecraft.client.input.MouseButtonEvent
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.NvgEvent
import org.cobalt.api.hud.HudAnchor
import org.cobalt.api.hud.HudModule
import org.cobalt.api.hud.HudModuleManager
import org.cobalt.api.ui.theme.ThemeManager
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.helper.HudConfig
import org.cobalt.internal.ui.UIScreen
import org.cobalt.internal.ui.hud.HudSettingsPopup
import org.cobalt.internal.ui.hud.SnapHelper
import org.cobalt.internal.ui.util.mouseX
import org.cobalt.internal.ui.util.mouseY

internal class UIHudEditor : UIScreen() {
  private var selectedModule: HudModule? = null
  private var dragging = false
  private var dragOffsetX = 0f
  private var dragOffsetY = 0f

  private val snapHelper = SnapHelper()
  private val settingsPopup = HudSettingsPopup()

  init {
    EventBus.register(this)
  }

  @Suppress("unused")
  @SubscribeEvent
  fun onRender(event: NvgEvent) {
    if (mc.screen != this) return

    val window = mc.window
    val width = window.screenWidth.toFloat()
    val height = window.screenHeight.toFloat()

    NVGRenderer.beginFrame(width, height)
    NVGRenderer.rect(0f, 0f, width, height, Color(0, 0, 0, 128).rgb)

    renderGrid(width, height)
    renderModuleBounds(width, height)
    renderGuides(width, height)
    settingsPopup.render()
    renderInstructions(width, height)
    NVGRenderer.endFrame()
  }

  private fun renderGrid(width: Float, height: Float) {
    val gridSize = 20f
    val gridColor = Color(255, 255, 255, 20).rgb
    var x = 0f
    while (x <= width) {
      NVGRenderer.line(x, 0f, x, height, 1f, gridColor)
      x += gridSize
    }
    var y = 0f
    while (y <= height) {
      NVGRenderer.line(0f, y, width, y, 1f, gridColor)
      y += gridSize
    }
  }

  private fun renderModuleBounds(width: Float, height: Float) {
    HudModuleManager.getModules().forEach { module ->
      val (sx, sy) = module.getScreenPosition(width, height)
      val w = module.getScaledWidth()
      val h = module.getScaledHeight()
      val isSelected = module == selectedModule
      val borderColor = if (isSelected) ThemeManager.currentTheme.accent else ThemeManager.currentTheme.controlBorder
      val borderThickness = if (isSelected) 2f else 1f
      NVGRenderer.hollowRect(sx, sy, w, h, borderThickness, borderColor, 4f)
      NVGRenderer.text(module.name, sx, sy + h + 6f, 12f, ThemeManager.currentTheme.textSecondary)
    }
  }

  private fun renderGuides(width: Float, height: Float) {
    if (!dragging) return
    snapHelper.activeGuides.forEach { guide ->
      if (guide.isVertical) {
        NVGRenderer.line(guide.position, 0f, guide.position, height, 1.5f, ThemeManager.currentTheme.accent)
      } else {
        NVGRenderer.line(0f, guide.position, width, guide.position, 1.5f, ThemeManager.currentTheme.accent)
      }
    }
  }

  private fun renderInstructions(width: Float, height: Float) {
    val text = "Left-click to select, drag to move | Right-click for settings | Scroll to resize | ESC to save and exit"
    val textWidth = NVGRenderer.textWidth(text, 12f)
    val padding = 14f
    val boxWidth = textWidth + padding * 2f
    val boxHeight = 26f
    val x = width / 2f - boxWidth / 2f
    val y = height - boxHeight - 20f
    NVGRenderer.rect(x, y, boxWidth, boxHeight, Color(0, 0, 0, 140).rgb, 8f)
    NVGRenderer.hollowRect(x, y, boxWidth, boxHeight, 1f, ThemeManager.currentTheme.controlBorder, 8f)
    NVGRenderer.text(text, x + padding, y + 7f, 12f, ThemeManager.currentTheme.textSecondary)
  }

  override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
    val screenWidth = mc.window.screenWidth.toFloat()
    val screenHeight = mc.window.screenHeight.toFloat()
    val mx = mouseX.toFloat()
    val my = mouseY.toFloat()
    val button = click.button()

    if (settingsPopup.visible) {
      if (settingsPopup.mouseClicked(mx, my, button)) return true
      if (!settingsPopup.containsPoint(mx, my)) settingsPopup.hide()
    }

    if (button == 1) {
      val target = findModuleUnderCursor(mx, my, screenWidth, screenHeight)
      if (target != null) {
        selectedModule = target
        settingsPopup.show(target, mx + 8f, my + 6f)
        return true
      }
    }

    if (button == 0) {
      val target = findModuleUnderCursor(mx, my, screenWidth, screenHeight)
      selectedModule = target
      if (target != null) {
        val (sx, sy) = target.getScreenPosition(screenWidth, screenHeight)
        dragOffsetX = mx - sx
        dragOffsetY = my - sy
        dragging = true
        settingsPopup.hide()
        return true
      }
    }

    return super.mouseClicked(click, doubled)
  }

  override fun mouseReleased(click: MouseButtonEvent): Boolean {
    if (click.button() == 0 && dragging) {
      dragging = false
      selectedModule?.let { module ->
        val screenWidth = mc.window.screenWidth.toFloat()
        val screenHeight = mc.window.screenHeight.toFloat()
        val (sx, sy) = module.getScreenPosition(screenWidth, screenHeight)
        val (snappedX, snappedY) = snapHelper.snapToGrid(sx, sy)
        updateModulePosition(module, snappedX, snappedY, screenWidth, screenHeight)
        snapHelper.clearGuides()
      }
      return true
    }
    return super.mouseReleased(click)
  }

  override fun mouseDragged(click: MouseButtonEvent, offsetX: Double, offsetY: Double): Boolean {
    if (click.button() != 0 || !dragging) return super.mouseDragged(click, offsetX, offsetY)

    val module = selectedModule ?: return super.mouseDragged(click, offsetX, offsetY)
    val screenWidth = mc.window.screenWidth.toFloat()
    val screenHeight = mc.window.screenHeight.toFloat()
    val mx = mouseX.toFloat()
    val my = mouseY.toFloat()
    val newScreenX = mx - dragOffsetX
    val newScreenY = my - dragOffsetY

    val otherBounds = HudModuleManager.getModules()
      .filter { it != module }
      .map {
        val (sx, sy) = it.getScreenPosition(screenWidth, screenHeight)
        SnapHelper.ModuleBounds(sx, sy, it.getScaledWidth(), it.getScaledHeight())
      }

    val (alignedX, alignedY) = snapHelper.findAlignmentGuides(
      newScreenX,
      newScreenY,
      module.getScaledWidth(),
      module.getScaledHeight(),
      screenWidth,
      screenHeight,
      otherBounds,
    )

    updateModulePosition(module, alignedX, alignedY, screenWidth, screenHeight)
    return true
  }

  override fun mouseScrolled(
    mouseX: Double,
    mouseY: Double,
    horizontalAmount: Double,
    verticalAmount: Double,
  ): Boolean {
    val screenWidth = mc.window.screenWidth.toFloat()
    val screenHeight = mc.window.screenHeight.toFloat()
    val target = findModuleUnderCursor(mouseX.toFloat(), mouseY.toFloat(), screenWidth, screenHeight)
    if (target != null) {
      target.scale = (target.scale + (if (verticalAmount > 0) 0.1f else -0.1f)).coerceIn(0.5f, 3.0f)
      return true
    }
    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
  }

  override fun init() {
    HudModuleManager.isEditorOpen = true
    super.init()
  }

  override fun onClose() {
    HudModuleManager.isEditorOpen = false
    HudConfig.save()
    EventBus.unregister(this)
    super.onClose()
  }

  private fun findModuleUnderCursor(
    mouseX: Float,
    mouseY: Float,
    screenWidth: Float,
    screenHeight: Float,
  ): HudModule? {
    return HudModuleManager.getModules().lastOrNull {
      it.containsPoint(mouseX, mouseY, screenWidth, screenHeight)
    }
  }

  private fun updateModulePosition(
    module: HudModule,
    newScreenX: Float,
    newScreenY: Float,
    screenWidth: Float,
    screenHeight: Float,
  ) {
    val w = module.getScaledWidth()
    val h = module.getScaledHeight()
    when (module.anchor) {
      HudAnchor.TOP_LEFT -> {
        module.offsetX = newScreenX
        module.offsetY = newScreenY
      }

      HudAnchor.TOP_CENTER -> {
        module.offsetX = newScreenX - (screenWidth / 2f - w / 2f)
        module.offsetY = newScreenY
      }

      HudAnchor.TOP_RIGHT -> {
        module.offsetX = screenWidth - w - newScreenX
        module.offsetY = newScreenY
      }

      HudAnchor.CENTER_LEFT -> {
        module.offsetX = newScreenX
        module.offsetY = newScreenY - (screenHeight / 2f - h / 2f)
      }

      HudAnchor.CENTER -> {
        module.offsetX = newScreenX - (screenWidth / 2f - w / 2f)
        module.offsetY = newScreenY - (screenHeight / 2f - h / 2f)
      }

      HudAnchor.CENTER_RIGHT -> {
        module.offsetX = screenWidth - w - newScreenX
        module.offsetY = newScreenY - (screenHeight / 2f - h / 2f)
      }

      HudAnchor.BOTTOM_LEFT -> {
        module.offsetX = newScreenX
        module.offsetY = screenHeight - h - newScreenY
      }

      HudAnchor.BOTTOM_CENTER -> {
        module.offsetX = newScreenX - (screenWidth / 2f - w / 2f)
        module.offsetY = screenHeight - h - newScreenY
      }

      HudAnchor.BOTTOM_RIGHT -> {
        module.offsetX = screenWidth - w - newScreenX
        module.offsetY = screenHeight - h - newScreenY
      }
    }
  }
}
