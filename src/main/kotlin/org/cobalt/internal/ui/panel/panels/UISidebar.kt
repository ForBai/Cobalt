package org.cobalt.internal.ui.panel.panels

import net.minecraft.client.Minecraft
import org.cobalt.api.ui.theme.ThemeManager
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.components.tooltips.TooltipPosition
import org.cobalt.internal.ui.components.tooltips.UITooltip
import org.cobalt.internal.ui.components.tooltips.impl.UITextTooltip
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.screen.UIConfig
import org.cobalt.internal.ui.screen.UIHudEditor
import org.cobalt.internal.ui.util.isHoveringOver

internal class UISidebar : UIPanel(
  x = 0F,
  y = 0F,
  width = 70F,
  height = 600F
) {

  private val moduleButton = UIButton("/assets/cobalt/textures/ui/box.svg") {
    UIConfig.swapBodyPanel(UIAddonList())
  }

  private val hudButton = UIButton("/assets/cobalt/icons/palette.svg") {
    UIHudEditor().openUI()
  }

  private val steveIcon = NVGRenderer.createImage("/assets/cobalt/textures/steve.png")
  private val userIcon = try {
    NVGRenderer.createImage("https://mc-heads.net/avatar/${Minecraft.getInstance().user.profileId}/100/face.png")
  } catch (_: Exception) {
    steveIcon
  }

  private val userIconTooltip = UITooltip(
    content = { UITextTooltip("Hello, ${Minecraft.getInstance().user.name}!") },
    position = TooltipPosition.BELOW
  )

  init {
    components.addAll(
      listOf(moduleButton, hudButton)
    )
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, ThemeManager.currentTheme.background, 10F)
    NVGRenderer.text("cb", x + width / 2F - 15F, y + 25F, 25F, ThemeManager.currentTheme.text)

    moduleButton
      .setSelected(true)
      .updateBounds(x + (width / 2F) - (moduleButton.width / 2F), y + 75F)
      .render()

    hudButton
      .setSelected(if (isHoveringOver(x + (width / 2F) - (hudButton.width / 2F), y + 75F + 35F, hudButton.width, hudButton.height)) true else false)
      .updateBounds(x + (width / 2F) - (hudButton.width / 2F), y + 75F + 35F)
      .render()

    val userIconX = x + (width / 2F) - 16F
    val userIconY = y + height - 32F - 20F

    NVGRenderer.image(
      userIcon,
      userIconX,
      userIconY,
      32F,
      32F,
      radius = 10F
    )

    userIconTooltip.updateBounds(userIconX, userIconY, 32F, 32F)
  }

  override fun mouseClicked(button: Int): Boolean {
    val userIconX = x + (width / 2F) - 16F
    val userIconY = y + height - 32F - 20F

    if (isHoveringOver(userIconX, userIconY, 32F, 32F) && button == 0) {
      UIConfig.swapBodyPanel(UIThemeSelector())
      return true
    }

    return super.mouseClicked(button)
  }

  private class UIButton(
    iconPath: String,
    private val onClick: () -> Unit,
  ) : UIComponent(0f, 0f, 22F, 22F) {

    val image = NVGRenderer.createImage(iconPath)
    private var selected = false

    fun setSelected(selected: Boolean): UIComponent {
      this.selected = selected
      return this
    }

    override fun render() {
      val hovering = isHoveringOver(x, y, width, height)

      NVGRenderer.image(
        image,
        x, y, width, height,

        colorMask = if (hovering || selected)
          ThemeManager.currentTheme.accent
        else
          ThemeManager.currentTheme.textSecondary
      )
    }

    override fun mouseClicked(button: Int): Boolean {
      if (isHoveringOver(x, y, width, height) && button == 0) {
        onClick.invoke()
        return true
      }

      return false
    }

  }

}
