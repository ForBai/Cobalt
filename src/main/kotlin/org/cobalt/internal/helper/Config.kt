package org.cobalt.internal.helper

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import net.minecraft.client.Minecraft
import org.cobalt.api.ui.theme.ThemeManager
import org.cobalt.api.ui.theme.impl.CustomTheme
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.ui.theme.ThemeSerializer

internal object Config {

  private val mc: Minecraft = Minecraft.getInstance()
  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val modulesFile = File(mc.gameDirectory, "config/cobalt/addons.json")
  private val themesFile = File(mc.gameDirectory, "config/cobalt/themes.json")

  fun loadModulesConfig() {
    loadThemesConfig()
    if (!modulesFile.exists()) {
      modulesFile.parentFile?.mkdirs()
      modulesFile.createNewFile()
      return
    }

    val text = modulesFile.bufferedReader().use { it.readText() }
    if (text.isEmpty()) return

    val addonsMap = AddonLoader.getAddons().associate { it.first.id to it.second }

    runCatching {
      JsonParser.parseString(text).asJsonArray
    }.getOrNull()?.forEach { element ->
      val addonObj = element.asJsonObject
      val addonId = addonObj.get("addon").asString
      val addon = addonsMap[addonId] ?: return@forEach

      val modulesMap = addon.getModules().associateBy { it.name }
      val settingsMap = modulesMap.values.flatMap { it.getSettings() }.associateBy { it.name }

      addonObj.getAsJsonArray("modules")?.forEach { moduleElement ->
        val moduleObj = moduleElement.asJsonObject
        val moduleName = moduleObj.get("name").asString
        modulesMap[moduleName] ?: return@forEach

        moduleObj.getAsJsonObject("settings")?.entrySet()?.forEach { (key, value) ->
          settingsMap[key]?.read(value)
        }
      }
    }
  }

  fun saveModulesConfig() {
    val jsonArray = JsonArray()

    AddonLoader.getAddons().forEach { (metadata, addon) ->
      val addonObject = JsonObject()
      addonObject.addProperty("addon", metadata.id)

      val modulesArray = JsonArray()
      addon.getModules().forEach { module ->
        val moduleObject = JsonObject()
        moduleObject.addProperty("name", module.name)

        val settingsObject = JsonObject()
        module.getSettings().forEach { setting ->
          settingsObject.add(setting.name, setting.write())
        }
        moduleObject.add("settings", settingsObject)
        modulesArray.add(moduleObject)
      }

      addonObject.add("modules", modulesArray)
      jsonArray.add(addonObject)
    }

    modulesFile.bufferedWriter().use { it.write(gson.toJson(jsonArray)) }
    saveThemesConfig()
  }

  private fun loadThemesConfig() {
    if (!themesFile.exists()) {
      themesFile.parentFile?.mkdirs()
      themesFile.createNewFile()
      return
    }

    val text = themesFile.bufferedReader().use { it.readText() }
    if (text.isEmpty()) return

    runCatching {
      JsonParser.parseString(text).asJsonObject
    }.getOrNull()?.let { root ->
      root.getAsJsonArray("themes")?.forEach { element ->
        ThemeManager.registerTheme(ThemeSerializer.fromJson(element.asJsonObject))
      }

      root.get("currentTheme")?.asString?.let { themeName ->
        ThemeManager.getThemes().firstOrNull { it.name == themeName }?.let {
          ThemeManager.setTheme(it)
        }
      }
    }
  }

  private fun saveThemesConfig() {
    val themeArray = JsonArray()
    ThemeManager.getThemes().forEach { theme ->
      if (theme is CustomTheme) {
        themeArray.add(ThemeSerializer.toJson(theme))
      }
    }

    val root = JsonObject()
    root.add("themes", themeArray)
    root.addProperty("currentTheme", ThemeManager.currentTheme.name)

    themesFile.parentFile?.mkdirs()
    themesFile.bufferedWriter().use { it.write(gson.toJson(root)) }
  }
}
