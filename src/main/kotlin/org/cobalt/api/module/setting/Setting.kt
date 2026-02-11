package org.cobalt.api.module.setting

import com.google.gson.JsonElement
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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
