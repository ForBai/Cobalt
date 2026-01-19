package org.cobalt.mixin.client;

import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import org.cobalt.internal.loader.AddonLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Superb mixin by oblongboot!
 */
@Mixin(CrashReport.class)
public abstract class AddonList_CrashReportMixin {

  @Inject(method = "getDetails(Ljava/lang/StringBuilder;)V", at = @At("HEAD"))
  private void addAddonInfo(StringBuilder crashReportBuilder, CallbackInfo ci) {
    String addons = AddonLoader.INSTANCE.getAddons().stream()
      .map(info -> info.getFirst().getName() + " v" + info.getFirst().getVersion())
      .collect(Collectors.joining(", "));

    if (addons.isEmpty()) {
      addons = "None";
    }

    int count = AddonLoader.INSTANCE.getAddons().size();

    crashReportBuilder
      .append("\n========================================")
      .append("\nCobalt Addons (")
      .append(count)
      .append("): ")
      .append(addons)
      .append("\n========================================\n");
  }

}
