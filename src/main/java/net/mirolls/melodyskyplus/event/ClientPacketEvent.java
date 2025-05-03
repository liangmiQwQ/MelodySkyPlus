package net.mirolls.melodyskyplus.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ClientPacketEvent extends Event {
  public Packet<?> packet;

  public ClientPacketEvent(Packet<?> packet) {
    this.packet = packet;
  }
}
