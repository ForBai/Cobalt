package org.cobalt.api.module.setting

import com.google.gson.JsonElement
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Base class for all module and HUD element settings.
 *
 * Settings are automatically serialized to/from the config file.
 * Inside a [Module][org.cobalt.api.module.Module], use `by` delegation:
 * ```
 * val speed by SliderSetting("Speed", "Movement speed", 1.0, 0.0, 5.0)
 * ```
 * Inside a [HudElementBuilder][org.cobalt.api.hud.HudElementBuilder], use `setting()` + `.value`:
 * ```
 * val speed = setting(SliderSetting("Speed", "Movement speed", 1.0, 0.0, 5.0))
 * // access via speed.value
 * ```
 *
 * @property name Display name shown in the settings UI.
 * @property description Tooltip/description shown in the settings UI.
 * @property value Current value. Updated by the UI and persisted automatically.
 */
abstract class Setting<T>(
  val name: String,
  val description: String,
  var value: T,
) : ReadWriteProperty<SettingsContainer, T>, PropertyDelegateProvider<SettingsContainer, ReadWriteProperty<SettingsContainer, T>> {

  override operator fun provideDelegate(thisRef: SettingsContainer, property: KProperty<*>): ReadWriteProperty<SettingsContainer, T> {
    thisRef.addSetting(this)
    return this
  }

  override operator fun getValue(thisRef: SettingsContainer, property: KProperty<*>): T {
    return value
  }

  override operator fun setValue(thisRef: SettingsContainer, property: KProperty<*>, value: T) {
    this.value = value
  }

  abstract fun read(element: JsonElement)
  abstract fun write(): JsonElement

}
