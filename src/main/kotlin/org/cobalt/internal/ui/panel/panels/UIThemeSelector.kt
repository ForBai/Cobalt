package org.cobalt.internal.ui.panel.panels

import org.cobalt.api.ui.theme.Theme
import org.cobalt.api.ui.theme.ThemeManager
import org.cobalt.api.ui.theme.impl.CustomTheme
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.components.UITopbar
import org.cobalt.internal.ui.panel.UIPanel
import org.cobalt.internal.ui.screen.UIConfig
import org.cobalt.internal.ui.util.GridLayout
import org.cobalt.internal.ui.util.ScrollHandler
import org.cobalt.internal.ui.util.isHoveringOver

internal class UIThemeSelector : UIPanel(
  x = 0F,
  y = 0F,
  width = 890F,
  height = 600F
) {

  private val topBar = UITopbar("Themes")
  private val allEntries = listOf(UICreateThemeEntry()) + ThemeManager.getThemes().map { UIThemeEntry(it) }
  private var entries = allEntries

  private val gridLayout = GridLayout(
    columns = 3,
    itemWidth = 270F,
    itemHeight = 100F,
    gap = 20F
  )

  private val scrollHandler = ScrollHandler()

  init {
    components.addAll(allEntries)
    components.add(topBar)
  }

  override fun render() {
    NVGRenderer.rect(x, y, width, height, ThemeManager.currentTheme.background, 10F)

    topBar
      .updateBounds(x, y)
      .render()


    val startY = y + topBar.height
    val visibleHeight = height - topBar.height

    scrollHandler.setMaxScroll(gridLayout.contentHeight(entries.size) + 20F, visibleHeight)
    NVGRenderer.pushScissor(x, startY, width, visibleHeight)

    val scrollOffset = scrollHandler.getOffset()
    gridLayout.layout(x + 20F, startY + 20F - scrollOffset, entries)
    entries.forEach(UIComponent::render)

    NVGRenderer.popScissor()
  }

  override fun mouseScrolled(horizontalAmount: Double, verticalAmount: Double): Boolean {
    if (isHoveringOver(x, y, width, height)) {
      scrollHandler.handleScroll(verticalAmount)
      return true
    }

    return false
  }

  private class UICreateThemeEntry : UIThemeEntry(CustomTheme("Create Theme")) {
    override fun render() {
      val hovering = isHoveringOver(x, y, width, height)

      NVGRenderer.rect(x, y, width, height, ThemeManager.currentTheme.panel, 10F)

      NVGRenderer.hollowRect(
        x, y, width, height,
        if (hovering) 2F else 1F,
        ThemeManager.currentTheme.accent,
        10F
      )

      NVGRenderer.text(
        "Create Theme",
        x + 20F,
        y + 20F,
        16F,
        ThemeManager.currentTheme.text
      )

      val swatchY = y + 50F
      val swatchSize = 30F
      val swatchGap = 10F

      NVGRenderer.rect(x + 20F, swatchY, swatchSize, swatchSize, ThemeManager.currentTheme.panel, 5F)
      NVGRenderer.hollowRect(x + 20F, swatchY, swatchSize, swatchSize, 1F, ThemeManager.currentTheme.textSecondary, 5F)

      NVGRenderer.rect(
        x + 20F + swatchSize + swatchGap,
        swatchY,
        swatchSize,
        swatchSize,
        ThemeManager.currentTheme.accent,
        5F
      )

      NVGRenderer.rect(
        x + 20F + (swatchSize + swatchGap) * 2,
        swatchY,
        swatchSize,
        swatchSize,
        ThemeManager.currentTheme.text,
        5F
      )
    }

    override fun mouseClicked(button: Int): Boolean {
      if (button != 0) return false

      if (isHoveringOver(x, y, width, height)) {
        handleSelection()
        return true
      }

      return false
    }

    override fun handleSelection() {
      val theme = CustomTheme(name = "Custom ${ThemeManager.getThemes().size + 1}")
      ThemeManager.registerTheme(theme)
      ThemeManager.setTheme(theme)
      UIConfig.swapBodyPanel(UIThemeEditor(theme))
    }
  }

  private open class UIThemeEntry(
    protected val theme: Theme,
  ) : UIComponent(
    x = 0F,
    y = 0F,
    width = 270F,
    height = 100F,
  ) {

    override fun render() {
      val hovering = isHoveringOver(x, y, width, height)
      val isSelected = ThemeManager.currentTheme.name == theme.name

      NVGRenderer.rect(
        x, y, width, height,
        theme.panel, 10F
      )

      if (isSelected) {
        NVGRenderer.hollowRect(
          x, y, width, height,
          2F, theme.accent, 10F
        )
      } else if (hovering) {
        NVGRenderer.hollowRect(
          x, y, width, height,
          1F, theme.accent, 10F
        )
      } else {
        NVGRenderer.hollowRect(
          x, y, width, height,
          1F, ThemeManager.currentTheme.controlBorder, 10F
        )
      }

      NVGRenderer.text(
        theme.name,
        x + 20F,
        y + 20F,
        16F,
        theme.text
      )

      // Color preview swatches
      val swatchY = y + 50F
      val swatchSize = 30F
      val swatchGap = 10F

      NVGRenderer.rect(x + 20F, swatchY, swatchSize, swatchSize, theme.background, 5F)
      NVGRenderer.hollowRect(x + 20F, swatchY, swatchSize, swatchSize, 1F, theme.textSecondary, 5F)

      NVGRenderer.rect(x + 20F + swatchSize + swatchGap, swatchY, swatchSize, swatchSize, theme.accent, 5F)

      NVGRenderer.rect(x + 20F + (swatchSize + swatchGap) * 2, swatchY, swatchSize, swatchSize, theme.text, 5F)
    }

    override fun mouseClicked(button: Int): Boolean {
      if (isHoveringOver(x, y, width, height) && button == 0) {
        handleSelection()
        return true
      }

      return false
    }

    open fun handleSelection() {
      if (theme is CustomTheme) {
        UIConfig.swapBodyPanel(UIThemeEditor(theme))
      } else {
        ThemeManager.setTheme(theme)
      }
    }

  }

}
