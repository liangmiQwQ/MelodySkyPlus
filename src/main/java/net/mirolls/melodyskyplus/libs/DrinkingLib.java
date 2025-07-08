package net.mirolls.melodyskyplus.libs;

import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DrinkingLib {
  Runnable task = null;

  public DrinkingLib() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onTick(PlayerUseItemEvent.Finish event) {
    if (event.item.getItem() == Items.potionitem && event.result.getItem() == Items.glass_bottle) {
      if (task != null) {
        task.run();
        task = null;
      }
    }
  }

  public void register(Runnable task) {
    this.task = task;
  }
}
