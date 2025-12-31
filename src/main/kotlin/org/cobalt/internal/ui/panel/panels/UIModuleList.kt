package org.cobalt.internal.ui.panel.panels

import java.awt.Color
import org.cobalt.api.addon.Addon
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.animation.ColorAnimation
import org.cobalt.internal.ui.components.UIAddonEntry
import org.cobalt.internal.ui.components.UITopbar
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.screen.UIConfig
import org.cobalt.internal.ui.util.isHoveringOver

internal class UIModuleList(
  private val metadata: AddonLoader.AddonMetadata,
  private val addon: Addon,
) : UIPanel(
  x = 0F,
  y = 0F,
  width = 890F,
  height = 600F
) {

  private val topBar = UITopbar("Modules")
  private val entries = AddonLoader.getAddons().map { UIAddonEntry(it.first, it.second) }

  private val backButton = UIBackButton()

  init {
    components.addAll(entries)
    components.addAll(
      listOf(backButton, topBar)
    )
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, Color(18, 18, 18).rgb, 10F)

    NVGRenderer.line(
      x + width / 4F,
      y + topBar.height / 2 + height * 1F / 8F,
      x + width / 4F,
      y + topBar.height / 2 + height * 7F / 8F,
      1F, Color(42, 42, 42).rgb
    )

    topBar
      .updateBounds(x, y)
      .render()

    backButton
      .updateBounds(x + 20F, y + topBar.height + 20F)
      .render()

    NVGRenderer.text(
      metadata.name,
      x + backButton.width + 35F,
      y + topBar.height + 27.5F,
      15F, Color(230, 230, 230).rgb
    )
  }

  private class UIBackButton : UIComponent(
    x = 0F,
    y = 0F,
    width = 30F,
    height = 30F
  ) {

    val leftArrow = NVGRenderer.createImage("/assets/cobalt/icons/arrow-left.svg")
    private val colorAnim = ColorAnimation(200L)
    private var wasHovering = false

    override fun render() {
      val hovering = isHoveringOver(x, y, width, height)

      if (hovering != wasHovering) {
        colorAnim.start()
        wasHovering = hovering
      }

      val bgColor = colorAnim.get(
        Color(42, 42, 42, 50),
        Color(61, 94, 149, 100),
        !hovering
      )

      val borderColor = colorAnim.get(
        Color(42, 42, 42),
        Color(61, 94, 149),
        !hovering
      )

      NVGRenderer.rect(x, y, width, height, bgColor.rgb, 5F)

      NVGRenderer.hollowRect(x, y, width, height, 2F, borderColor.rgb, 5F)

      NVGRenderer.image(
        leftArrow,
        x + width / 2F - 10F,
        y + height / 2F - 10F,
        20F, 20F, 0F,
        borderColor.rgb
      )
    }

    override fun mouseClicked(button: Int): Boolean {
      if (isHoveringOver(x, y, width, height) && button == 0) {
        UIConfig.swapBodyPanel(UIAddonList())
      }

      return false
    }

  }

}
