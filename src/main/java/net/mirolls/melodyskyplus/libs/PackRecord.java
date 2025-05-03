package net.mirolls.melodyskyplus.libs;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mirolls.melodyskyplus.event.ServerPacketEvent;

public class PackRecord {

  private long lastPacketTime;
  private Vec3 lastPacketPosition;

  public PackRecord() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onReceivePacket(ServerPacketEvent event) {
    Minecraft mc = Minecraft.getMinecraft();

    if (mc.thePlayer == null || mc.theWorld == null) return;
    if (!(event.packet instanceof S03PacketTimeUpdate)) return;
    lastPacketTime = System.currentTimeMillis();
    lastPacketPosition = mc.thePlayer.getPositionVector();
  }

  public long getLastPacketTime() {
    return lastPacketTime;
  }

  public Vec3 getLastPacketPosition() {
    return lastPacketPosition;
  }
}
