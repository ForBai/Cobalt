package org.cobalt.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.cobalt.api.event.impl.client.BlockChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
abstract class BreakBlock_ClientPlayerInteractionManagerMixin {
    
    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void onBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null) {
            BlockState oldBlock = mc.world.getBlockState(pos);
            BlockState newBlock = Blocks.AIR.getDefaultState();
            new BlockChangeEvent(pos, oldBlock, newBlock).post();
        }
    }
}