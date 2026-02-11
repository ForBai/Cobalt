package org.cobalt.internal.helper

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import net.minecraft.client.Minecraft
import org.cobalt.api.hud.HudAnchor
import org.cobalt.api.hud.HudModuleManager

internal object HudConfig {

  private val mc: Minecraft = Minecraft.getInstance()
  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val hudFile = File(mc.gameDirectory, "config/cobalt/hud.json")

  fun load() {
    if (!hudFile.exists()) {
      hudFile.parentFile?.mkdirs()
      hudFile.createNewFile()
      return
    }

    val text = hudFile.bufferedReader().use { it.readText() }
    if (text.isEmpty()) return

    runCatching {
      val root = JsonParser.parseString(text).asJsonObject
      val modulesObj = root.getAsJsonObject("modules") ?: return

       for ((id, element) in modulesObj.entrySet()) {
         val module = HudModuleManager.getModule(id) ?: continue
         val obj = element.asJsonObject

         module.enabled = obj.get("enabled")?.asBoolean ?: true
         module.anchor = obj.get("anchor")?.asString?.let {
           runCatching { HudAnchor.valueOf(it) }.getOrNull()
         } ?: HudAnchor.TOP_LEFT
         module.offsetX = obj.get("offsetX")?.asFloat ?: 10f
         module.offsetY = obj.get("offsetY")?.asFloat ?: 10f
         module.scale = obj.get("scale")?.asFloat?.coerceIn(0.5f, 3.0f) ?: 1.0f

         // Load settings
         val settingsObj = obj.getAsJsonObject("settings")
         if (settingsObj != null) {
           module.getSettings().forEach { setting ->
             settingsObj.get(setting.name)?.let { element ->
               runCatching { setting.read(element) }
             }
           }
         }
       }
    }
  }

  fun save() {
    val root = JsonObject()
    val modulesObj = JsonObject()

    HudModuleManager.getModules().forEach { module ->
      val obj = JsonObject().apply {
        addProperty("enabled", module.enabled)
        addProperty("anchor", module.anchor.name)
        addProperty("offsetX", module.offsetX)
        addProperty("offsetY", module.offsetY)
        addProperty("scale", module.scale)
        
        // Add settings persistence
        val settingsObj = JsonObject()
        module.getSettings().forEach { setting ->
          settingsObj.add(setting.name, setting.write())
        }
        add("settings", settingsObj)
      }
      modulesObj.add(module.id, obj)
    }

    root.add("modules", modulesObj)

    hudFile.parentFile?.mkdirs()
    hudFile.bufferedWriter().use { it.write(gson.toJson(root)) }
  }
}
