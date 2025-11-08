package net.mirolls.melodyskyplus.libs;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Client;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;

public class MiningSkillExecutor {
  private final ArrayList<Module> mods = new ArrayList<>();
  private int tick = 20;
  private int oldItem = 0;

  public MiningSkillExecutor() {
    MinecraftForge.EVENT_BUS.register(this);
    EventBus.getInstance().register(this);
  }

  @EventHandler
  public void onTick(EventPreUpdate event) {
    Minecraft mc = Minecraft.getMinecraft();
    if (tick == 0) {
      oldItem = mc.thePlayer.inventory.currentItem;
      for (int i = 0; i < 9; ++i) {
        ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
        if (item != null && item.getItem().getRegistryName().toLowerCase().contains("rod")) {
          mc.thePlayer.inventory.currentItem = i;
        }
      }
    }
    if (tick == 3) {
      Client.rightClick();
    }
    if (tick == 5) {
      mc.thePlayer.inventory.currentItem = oldItem;
    }
    if (tick == 15) {
      Client.rightClick();
    }
    if (tick == 20) {
      reEnableMacros();
    }
    tick++;
  }

  public void start() {
    MelodySkyPlus.LOGGER.info("has run start.");
    if (tick > 10) {
      disableMacros();
      tick = 0;
    }
  }

  private void disableMacros() {
    for (Module mod : ModuleManager.getModulesInType(ModuleType.Mining)) {
      if (mod.isEnabled() && !mod.excepted) {
        mod.setEnabled(false);
        this.mods.add(mod);
      }
    }
  }

  private void reEnableMacros() {
    for (Module mod : this.mods) {
      mod.setEnabled(true);
    }

    this.mods.clear();
  }
}
