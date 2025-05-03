package net.mirolls.melodyskyplus.mixin.minecraft;

import io.netty.channel.ChannelHandlerContext;
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
    } else if (packet.getClass().getSimpleName().startsWith("C")) {
      MinecraftForge.EVENT_BUS.post(new ClientPacketEvent(packet));
    }
  }
}
