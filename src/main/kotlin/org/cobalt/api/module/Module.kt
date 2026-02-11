package org.cobalt.api.module

import org.cobalt.api.module.setting.Setting
import org.cobalt.api.module.setting.SettingsContainer

abstract class Module(val name: String) : SettingsContainer {

  private val settingsList = mutableListOf<Setting<*>>()

  override fun addSetting(vararg settings: Setting<*>) {
    settingsList.addAll(listOf(*settings))
  }

  override fun getSettings(): List<Setting<*>> {
    return settingsList
  }

}
