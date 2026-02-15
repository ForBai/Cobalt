package org.cobalt.api.ui.theme

import org.cobalt.api.ui.theme.impl.DarkTheme
import org.cobalt.api.ui.theme.impl.LightTheme

object ThemeManager {

  private val themes = mutableListOf<Theme>()
  var currentTheme: Theme = DarkTheme()
    private set

  init {
    registerTheme(DarkTheme())
    registerTheme(LightTheme())
  }

  fun registerTheme(theme: Theme) {
    if (themes.none { it.name == theme.name }) {
      themes.add(theme)
    }
  }

  fun setTheme(theme: Theme) {
    currentTheme = theme
  }

  fun getThemes(): List<Theme> {
    return themes
  }

  fun unregisterTheme(theme: Theme): Boolean {
    // Prevent deleting built-in themes
    if (theme.name == "Dark" || theme.name == "Light") return false

    val removed = themes.removeIf { it.name == theme.name }

    // If deleted theme was current, switch to DarkTheme
    if (removed && currentTheme.name == theme.name) {
      currentTheme = themes.first { it.name == "Dark" }
    }

    return removed
  }

}
