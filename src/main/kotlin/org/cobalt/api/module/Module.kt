package org.cobalt.api.module

import org.cobalt.api.hud.HudElement
import org.cobalt.api.module.setting.Setting
import org.cobalt.api.module.setting.SettingsContainer

abstract class Module(val name: String) : SettingsContainer {

  private val settingsList = mutableListOf<Setting<*>>()
  private val hudElementsList = mutableListOf<HudElement>()

  override fun addSetting(vararg settings: Setting<*>) {
    settingsList.addAll(listOf(*settings))
  }

  override fun getSettings(): List<Setting<*>> {
    return settingsList
  }

  fun addHudElement(element: HudElement) {
    hudElementsList.add(element)
  }

  fun getHudElements(): List<HudElement> {
    return hudElementsList
  }

}
