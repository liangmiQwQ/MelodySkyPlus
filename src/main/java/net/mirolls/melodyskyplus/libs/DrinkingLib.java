package net.mirolls.melodyskyplus.libs;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.Melody.Utils.Helper;

public class DrinkingLib {
  public DrinkingLib() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onTick(PlayerUseItemEvent.Finish event) {
    Helper.sendMessage(event.item.getItem().getRegistryName());
    Helper.sendMessage(event.result.getItem().getRegistryName());
  }
}
