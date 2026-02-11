package org.cobalt.api.module.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.cobalt.api.module.setting.Setting

/** ARGB color picker setting. Value is an ARGB integer (e.g. `0xFFFF0000.toInt()` for red). */
class ColorSetting(
  name: String,
  description: String,
  defaultValue: Int,
) : Setting<Int>(name, description, defaultValue) {

  override fun read(element: JsonElement) {
    this.value = element.asInt
  }

  override fun write(): JsonElement {
    return JsonPrimitive(value)
  }

}
