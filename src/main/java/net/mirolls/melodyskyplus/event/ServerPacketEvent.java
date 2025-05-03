package net.mirolls.melodyskyplus.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ServerPacketEvent extends Event {
  public Packet<?> packet;

  public ServerPacketEvent(Packet<?> packet) {
    this.packet = packet;
  }
}
