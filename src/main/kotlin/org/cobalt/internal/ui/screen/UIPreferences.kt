package org.cobalt.internal.ui.screen

import java.io.File
import net.minecraft.client.Minecraft
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object UIPreferences {
    private val mc = Minecraft.getInstance()
    private val gson = Gson()
    private val preferencesFile by lazy { 
        File(mc.gameDirectory, "config/cobalt/preferences.json")
    }
    
    var isCustomHomescreenEnabled: Boolean = true
        private set
        
    fun setCustomHomescreenEnabled(enabled: Boolean) {
        isCustomHomescreenEnabled = enabled
        save()
    }
    
    fun load() {
        if (!preferencesFile.exists()) return
        
        try {
            val json = JsonParser.parseString(preferencesFile.readText()).asJsonObject
            if (json.has("customHomescreen")) {
                isCustomHomescreenEnabled = json.get("customHomescreen").asBoolean
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun save() {
        try {
            val json = JsonObject()
            json.addProperty("customHomescreen", isCustomHomescreenEnabled)
            
            preferencesFile.parentFile?.mkdirs()
            preferencesFile.writeText(gson.toJson(json))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
