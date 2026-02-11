package org.cobalt.api.module.setting

interface SettingsContainer {

  fun addSetting(vararg settings: Setting<*>)

  fun getSettings(): List<Setting<*>>

}
