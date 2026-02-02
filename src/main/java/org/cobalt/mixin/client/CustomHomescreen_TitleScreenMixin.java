package org.cobalt.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.cobalt.internal.ui.screen.UIHomescreen;
import org.cobalt.internal.ui.screen.UIPreferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class CustomHomescreen_TitleScreenMixin extends Screen {

    protected CustomHomescreen_TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void init(CallbackInfo ci) {
        if (UIPreferences.INSTANCE.isCustomHomescreenEnabled()) {
            UIHomescreen.INSTANCE.init();
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injectCobaltButton(CallbackInfo ci) {
        if (!UIPreferences.INSTANCE.isCustomHomescreenEnabled()) {
            this.addRenderableWidget(Button.builder(
                Component.literal("Cobalt"),
                button -> {
                    UIPreferences.INSTANCE.setCustomHomescreenEnabled(true);
                    this.minecraft.setScreen(this);
                }
            ).pos(this.width - 80, 10)
             .size(70, 20)
             .build());
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (UIPreferences.INSTANCE.isCustomHomescreenEnabled()) {
            UIHomescreen.INSTANCE.render((float)this.width, (float)this.height, (double)mouseX, (double)mouseY);
            ci.cancel();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (UIPreferences.INSTANCE.isCustomHomescreenEnabled()) {
            if (UIHomescreen.INSTANCE.mouseClicked(event)) {
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }
}
