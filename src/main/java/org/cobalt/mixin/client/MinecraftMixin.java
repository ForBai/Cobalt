package org.cobalt.mixin.client;

import java.util.List;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import org.cobalt.api.addon.Addon;
import org.cobalt.api.addon.AddonMetadata;
import org.cobalt.api.event.impl.client.TickEvent;
import org.cobalt.internal.loader.AddonLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

  @Inject(at = @At("HEAD"), method = "tick")
  private void onStartTick(CallbackInfo callbackInfo) {
    TickEvent.Start startTickEvent = new TickEvent.Start();
    startTickEvent.post();
  }

  @Inject(at = @At("RETURN"), method = "tick")
  private void onEndTick(CallbackInfo callbackInfo) {
    TickEvent.End endTickEvent = new TickEvent.End();
    endTickEvent.post();
  }

  @Inject(method = "close", at = @At("HEAD"))
  public void onClose(CallbackInfo callbackInfo) {
    List<Pair<AddonMetadata, Addon>> addonsList = AddonLoader.INSTANCE.getAddons();

    addonsList.forEach((addon) -> {
      addon.getSecond().onUnload();
    });
  }

}
