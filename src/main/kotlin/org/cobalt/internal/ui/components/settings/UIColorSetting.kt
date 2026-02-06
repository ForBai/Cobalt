package org.cobalt.internal.ui.components.settings

import java.awt.Color
import org.cobalt.api.module.setting.impl.ColorSetting
import org.cobalt.api.ui.theme.ThemeManager
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.api.util.ui.helper.Gradient
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.util.isHoveringOver
import org.cobalt.internal.ui.util.mouseX
import org.cobalt.internal.ui.util.mouseY

internal class UIColorSetting(private val setting: ColorSetting) : UIComponent(
  x = 0F,
  y = 0F,
  width = 627.5F,
  height = 60F,
) {

  private var pickerOpen = false
  private var hue = 0f
  private var saturation = 1f
  private var lightness = 0.5f
  private var opacity = 1f

  private var draggingHue = false
  private var draggingOpacity = false
  private var draggingColor = false

  init {
    val color = Color(setting.value)
    val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)

    hue = hsb[0]
    saturation = hsb[1]
    lightness = hsb[2]
    opacity = color.alpha / 255f
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, ThemeManager.currentTheme.controlBg, 10F)
    NVGRenderer.hollowRect(x, y, width, height, 1F, ThemeManager.currentTheme.controlBorder, 10F)

    NVGRenderer.text(setting.name, x + 20F, y + (height / 2F) - 15.5F, 15F, ThemeManager.currentTheme.text)
    NVGRenderer.text(setting.description, x + 20F, y + (height / 2F) + 2F, 12F, ThemeManager.currentTheme.textSecondary)

    NVGRenderer.rect(x + width - 50F, y + (height / 2F) - 15F, 30F, 30F, setting.value, 6F)
    NVGRenderer.hollowRect(x + width - 50F, y + (height / 2F) - 15F, 30F, 30F, 1.5F, ThemeManager.currentTheme.controlBorder, 6F)
  }

  fun drawColorPicker() {
    if (!pickerOpen) return

    val px = x + width - 220F
    val py = y + height - 10F
    val bx = px + 10F
    val by = py + 10F
    val size = 180F

    NVGRenderer.rect(px, py, 200F, 250F, ThemeManager.currentTheme.panel, 8F)
    NVGRenderer.hollowRect(px, py, 200F, 250F, 2F, ThemeManager.currentTheme.controlBorder, 8F)

    val hueColor = Color.HSBtoRGB(hue, 1f, 1f)

    NVGRenderer.rect(bx, by, size, size, hueColor, 0F)
    NVGRenderer.gradientRect(
      bx,
      by,
      size,
      size,
      ThemeManager.currentTheme.white,
      ThemeManager.currentTheme.transparent,
      Gradient.LeftToRight,
      0F
    )
    NVGRenderer.gradientRect(
      bx,
      by,
      size,
      size,
      ThemeManager.currentTheme.transparent,
      ThemeManager.currentTheme.black,
      Gradient.TopToBottom,
      0F
    )
    NVGRenderer.hollowRect(bx, by, size, size, 1F, ThemeManager.currentTheme.controlBorder, 0F)

    val selectorX = bx + saturation * size
    val selectorY = by + (1f - lightness) * size

    NVGRenderer.circle(selectorX, selectorY, 5F, ThemeManager.currentTheme.white)
    NVGRenderer.circle(selectorX, selectorY, 3F, ThemeManager.currentTheme.black)

    val hueY = py + size + 20F

    for (i in 0..5) {
      val x1 = bx + (size / 6f) * i
      val x2 = bx + (size / 6f) * (i + 1)
      val color1 = Color.HSBtoRGB(i / 6f, 1f, 1f)
      val color2 = Color.HSBtoRGB((i + 1) / 6f, 1f, 1f)

      NVGRenderer.gradientRect(x1, hueY, x2 - x1, 15F, color1, color2, Gradient.LeftToRight, 0F)
    }

    NVGRenderer.hollowRect(bx, hueY, size, 15F, 1F, ThemeManager.currentTheme.controlBorder, 0F)
    NVGRenderer.rect(bx + hue * size - 2F, hueY - 2F, 4F, 19F, ThemeManager.currentTheme.white, 1F)

    val opacityY = hueY + 25F

    NVGRenderer.rect(bx, opacityY, size, 15F, ThemeManager.currentTheme.white, 0F)

    for (i in 0..17) {
      if (i % 2 == 0) {
        NVGRenderer.rect(bx + i * 10F, opacityY, 10F, 7.5F, ThemeManager.currentTheme.textSecondary, 0F)
        NVGRenderer.rect(bx + i * 10F, opacityY + 7.5F, 10F, 7.5F, ThemeManager.currentTheme.white, 0F)
      } else {
        NVGRenderer.rect(bx + i * 10F, opacityY, 10F, 7.5F, ThemeManager.currentTheme.white, 0F)
        NVGRenderer.rect(bx + i * 10F, opacityY + 7.5F, 10F, 7.5F, ThemeManager.currentTheme.textSecondary, 0F)
      }
    }

    val currentColor = Color.HSBtoRGB(hue, saturation, lightness)
    val opaqueColor = Color(currentColor or (255 shl 24), true).rgb
    val transparentColor = Color(currentColor and 0x00FFFFFF, true).rgb

    NVGRenderer.gradientRect(bx, opacityY, size, 15F, transparentColor, opaqueColor, Gradient.LeftToRight, 0F)
    NVGRenderer.hollowRect(bx, opacityY, size, 15F, 1F, ThemeManager.currentTheme.controlBorder, 0F)
    NVGRenderer.rect(bx + opacity * size - 2F, opacityY - 2F, 4F, 19F, ThemeManager.currentTheme.white, 1F)
  }

  override fun mouseClicked(button: Int): Boolean {
    if (button != 0) return false

    val buttonX = x + width - 50F
    val buttonY = y + (height / 2F) - 15F

    if (isHoveringOver(buttonX, buttonY, 30F, 30F)) {
      pickerOpen = !pickerOpen
      return true
    }

    if (!pickerOpen) {
      return false
    }

    val px = x + width - 220F
    val py = y + height - 10F
    val bx = px + 10F
    val by = py + 10F

    if (isHoveringOver(bx, by, 180F, 180F)) {
      draggingColor = true
      updateColorFromBox(bx, by)
      return true
    }

    val hueY = py + 200F

    if (isHoveringOver(bx, hueY, 180F, 15F)) {
      draggingHue = true
      updateHueFromSlider(bx)
      return true
    }

    val opacityY = hueY + 25F

    if (isHoveringOver(bx, opacityY, 180F, 15F)) {
      draggingOpacity = true
      updateOpacityFromSlider(bx)
      return true
    }

    return isHoveringOver(px, py, 200F, 250F).also {
      if (!it) pickerOpen = false
    }
  }

  override fun mouseReleased(button: Int): Boolean {
    if (button == 0) {
      draggingHue = false
      draggingOpacity = false
      draggingColor = false
    }

    return false
  }

  override fun mouseDragged(button: Int, offsetX: Double, offsetY: Double): Boolean {
    if (button != 0 || !pickerOpen) return false

    val px = x + width - 220F
    val py = y + height - 10F
    val bx = px + 10F

    when {
      draggingColor -> updateColorFromBox(bx, py + 10F)
      draggingHue -> updateHueFromSlider(bx)
      draggingOpacity -> updateOpacityFromSlider(bx)
      else -> return false
    }

    return true
  }

  private fun updateColorFromBox(boxX: Float, boxY: Float) {
    saturation = ((mouseX.toFloat() - boxX) / 180F).coerceIn(0f, 1f)
    lightness = (1f - (mouseY.toFloat() - boxY) / 180F).coerceIn(0f, 1f)
    updateColor()
  }

  private fun updateHueFromSlider(sliderX: Float) {
    hue = ((mouseX.toFloat() - sliderX) / 180F).coerceIn(0f, 1f)
    updateColor()
  }

  private fun updateOpacityFromSlider(sliderX: Float) {
    opacity = ((mouseX.toFloat() - sliderX) / 180F).coerceIn(0f, 1f)
    updateColor()
  }

  private fun updateColor() {
    val rgb = Color.HSBtoRGB(hue, saturation, lightness)
    val alpha = (opacity * 255).toInt()
    setting.value = (alpha shl 24) or (rgb and 0x00FFFFFF)
  }

}
