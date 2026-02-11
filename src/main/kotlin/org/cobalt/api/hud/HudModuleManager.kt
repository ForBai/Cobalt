package org.cobalt.api.hud

import net.minecraft.client.Minecraft
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.NvgEvent
import org.cobalt.api.hud.modules.WatermarkModule
import org.cobalt.api.util.ui.NVGRenderer

object HudModuleManager {

  private val mc: Minecraft = Minecraft.getInstance()
  private val modules = mutableListOf<HudModule>()

  @Volatile
  var isEditorOpen: Boolean = false

  init {
    register(WatermarkModule())
  }

  fun register(module: HudModule) {
    if (modules.none { it.id == module.id }) {
      modules.add(module)
    }
  }

  fun unregister(id: String) {
    modules.removeIf { it.id == id }
  }

  fun getModules(): List<HudModule> = modules.toList()

  fun getModule(id: String): HudModule? = modules.find { it.id == id }

  fun resetAllPositions() {
    modules.forEach { it.resetPosition() }
  }

  @Suppress("unused")
  @SubscribeEvent
  fun onRender(event: NvgEvent) {
    if (mc.screen != null && !isEditorOpen) return

    val window = mc.window
    val screenWidth = window.screenWidth.toFloat()
    val screenHeight = window.screenHeight.toFloat()

    NVGRenderer.beginFrame(screenWidth, screenHeight)

    modules.filter { it.enabled }.forEach { module ->
      val (screenX, screenY) = module.getScreenPosition(screenWidth, screenHeight)

      NVGRenderer.push()
      NVGRenderer.translate(screenX, screenY)
      NVGRenderer.scale(module.scale, module.scale)
      module.render(0f, 0f, module.scale)
      NVGRenderer.pop()
    }

    NVGRenderer.endFrame()
  }
}
