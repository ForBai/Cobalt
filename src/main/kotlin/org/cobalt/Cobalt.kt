package org.cobalt

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import org.cobalt.api.command.CommandManager
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.client.TickEvent
import org.cobalt.api.event.impl.render.WorldRenderEvent
import org.cobalt.api.module.ModuleManager
import org.cobalt.api.notification.NotificationManager
import org.cobalt.api.pathfinder.IPathExec
import org.cobalt.api.util.TickScheduler
import org.cobalt.api.util.rotation.IRotationExec
import org.cobalt.internal.command.MainCommand
import org.cobalt.internal.helper.Config
import org.cobalt.internal.loader.AddonLoader
import org.cobalt.internal.rotation.RotationExec

@Suppress("UNUSED")
object Cobalt : ClientModInitializer {

  private val mc: MinecraftClient =
    MinecraftClient.getInstance()

  override fun onInitializeClient() {
    AddonLoader.getAddons().map { it.second }.forEach {
      it.onLoad()
      ModuleManager.addModules(it.getModules())
    }

    CommandManager.register(MainCommand)
    CommandManager.dispatchAll()

    listOf(TickScheduler, MainCommand, NotificationManager).forEach { EventBus.register(it) }

    Config.loadModulesConfig()
    EventBus.register(this)
    println("Cobalt Mod Initialized")
  }

  @SubscribeEvent
  fun onWorldRenderLast(event: WorldRenderEvent.Last) {
    mc.player?.let {
      RotationExec.onRotate(it)
    }
  }

}
