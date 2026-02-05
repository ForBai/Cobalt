package org.cobalt.mixin.network;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.cobalt.api.event.impl.client.ChatEvent;
import org.cobalt.api.event.impl.client.PacketEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class PacketEvent_ConnectionMixin {

  @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
  private static void onPacketReceived(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
    PacketEvent.Incoming event = new PacketEvent.Incoming(packet);
    event.post();

    if (event.isCancelled()) {
      ci.cancel();
      return;
    }

    if (packet instanceof ClientboundSystemChatPacket) {
      new ChatEvent.Receive(packet).post();
    }
  }

  @Inject(method = "sendPacket", at = @At("HEAD"))
  private void onPacketSent(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
    PacketEvent.Outgoing event = new PacketEvent.Outgoing(packet);
    event.post();

    if (event.isCancelled()) {
      ci.cancel();
      return;
    }

    if (packet instanceof ServerboundChatPacket) {
      new ChatEvent.Send(packet).post();
    }
  }

}
