package org.cobalt.api.hud.modules

import org.cobalt.api.hud.HudAnchor
import org.cobalt.api.hud.HudModule
import org.cobalt.api.module.setting.impl.TextSetting
import org.cobalt.api.module.setting.impl.ColorSetting
import org.cobalt.api.module.setting.impl.CheckboxSetting
import org.cobalt.api.ui.theme.ThemeManager
import org.cobalt.api.util.ui.NVGRenderer

class WatermarkModule : HudModule(
  id = "watermark",
  name = "Watermark",
  description = "Displays Cobalt branding",
) {

  override val defaultAnchor = HudAnchor.TOP_LEFT
  override val defaultOffsetX = 10f
  override val defaultOffsetY = 10f
  override val defaultScale = 1.0f

  private val textSize = 18f
  
  private val textSetting = TextSetting("Text", "Display text", "Cobalt")
  private val colorSetting = ColorSetting("Color", "Text color", ThemeManager.currentTheme.accent)
  private val shadowSetting = CheckboxSetting("Shadow", "Show text shadow", false)
  private val backgroundSetting = CheckboxSetting("Background", "Show background box", false)

  init {
    addSetting(textSetting, colorSetting, shadowSetting, backgroundSetting)
    resetPosition()
  }

  override fun getBaseWidth(): Float = NVGRenderer.textWidth(textSetting.value, textSize) + (if (backgroundSetting.value) 16f else 0f)

  override fun getBaseHeight(): Float = textSize + (if (backgroundSetting.value) 12f else 4f)

  override fun render(screenX: Float, screenY: Float, scale: Float) {
    if (backgroundSetting.value) {
      NVGRenderer.rect(screenX - 8f, screenY - 6f, 
        getBaseWidth(), getBaseHeight(), 
        ThemeManager.currentTheme.panel, 6f)
    }
    
    if (shadowSetting.value) {
      NVGRenderer.textShadow(textSetting.value, screenX, screenY, textSize, colorSetting.value)
    } else {
      NVGRenderer.text(textSetting.value, screenX, screenY, textSize, colorSetting.value)
    }
  }
}
