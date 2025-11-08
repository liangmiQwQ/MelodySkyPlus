package net.mirolls.melodyskyplus.mixin.minecraft;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import net.mirolls.melodyskyplus.event.ClientPacketEvent;
import net.mirolls.melodyskyplus.event.ServerPacketEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class NetWorkManagerMixin {
  @Inject(method = "channelRead0*", at = @At("HEAD"))
  private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
    if (packet.getClass().getSimpleName().startsWith("S")) {
      MinecraftForge.EVENT_BUS.post(new ServerPacketEvent(packet));
    }
  }

  @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"))
  private void sendPacket(Packet<?> packet, CallbackInfo ci) {
    if (packet.getClass().getSimpleName().startsWith("C")) {
      MinecraftForge.EVENT_BUS.post(new ClientPacketEvent(packet));
    }
  }

  @Inject(
      method =
          "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)V",
      at = @At("HEAD"))
  private void sendPacket(
      Packet<?> p_sendPacket_1_,
      GenericFutureListener<? extends Future<? super Void>> p_sendPacket_2_,
      GenericFutureListener<? extends Future<? super Void>>[] p_sendPacket_3_,
      CallbackInfo ci) {
    if (p_sendPacket_1_.getClass().getSimpleName().startsWith("C")) {
      MinecraftForge.EVENT_BUS.post(new ClientPacketEvent(p_sendPacket_1_));
    }
  }
}
