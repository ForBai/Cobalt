package org.cobalt.internal.helper

import com.google.gson.*
import java.io.File
import org.cobalt.Cobalt
import org.cobalt.api.module.ModuleManager
import org.cobalt.internal.loader.AddonLoader

internal object Config {

  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val modulesFile = File(Cobalt.mc.runDirectory, "config/cobalt/addons.json")

  fun loadModulesConfig() {
    if (!modulesFile.exists()) {
      modulesFile.parentFile.mkdirs()
      modulesFile.createNewFile()
    }

    val text = modulesFile.bufferedReader().use { it.readText() }
    if (text.isEmpty()) return

    val jsonArray = JsonParser.parseString(text).asJsonArray

    for (element in jsonArray) {
      val addonObj = element.asJsonObject
      val addonId = addonObj.get("addon").asString
      val modulesArray = addonObj.getAsJsonArray("modules")

      val addon = AddonLoader.getAddons().find { it.first.id == addonId }?.second ?: continue

      for (moduleElement in modulesArray) {
        val moduleObj = moduleElement.asJsonObject
        val moduleName = moduleObj.get("name").asString

        val module = addon.getModules().find { it.name == moduleName } ?: continue

        val settingsObj = moduleObj.getAsJsonObject("settings")
        if (settingsObj != null) {
          for ((key, value) in settingsObj.entrySet()) {
            val setting = module.getSettings().find { it.name == key } ?: continue
            setting.read(value)
          }
        }
      }
    }
  }

  fun saveModulesConfig() {
    val jsonArray = JsonArray()

    for ((metadata, addon) in AddonLoader.getAddons()) {
      val addonObj = JsonObject()
      addonObj.add("addon", JsonPrimitive(metadata.id))

      val modulesArray = JsonArray()
      for (module in addon.getModules()) {
        val moduleObj = JsonObject()
        moduleObj.add("name", JsonPrimitive(module.name))
        moduleObj.add("settings", JsonObject().apply {
          module.getSettings().forEach {
            add(it.name, it.write())
          }
        })
        modulesArray.add(moduleObj)
      }

      addonObj.add("modules", modulesArray)
      jsonArray.add(addonObj)
    }

    modulesFile.bufferedWriter().use {
      it.write(gson.toJson(jsonArray))
    }
  }

}
