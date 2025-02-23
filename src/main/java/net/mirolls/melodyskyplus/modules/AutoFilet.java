package net.mirolls.melodyskyplus.modules;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.game.item.ItemUtils;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.injection.mixins.gui.GuiPlayerTabAccessor;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class AutoFilet extends Module {
  private static AutoFilet INSTANCE;
  private final TimerUtil ticksTimer;
  private final TimerUtil coolDownTimer;
  private final List<Module> mods;
  private int fishTick;
  private int prevItem;
  private int tick;

  public AutoFilet() {
    super("AutoFilet", ModuleType.Mining);
    this.mods = new ArrayList<>();
    this.fishTick = -1;
    this.tick = 0;
    this.ticksTimer = (new TimerUtil()).reset();
    this.coolDownTimer = (new TimerUtil()).reset();
    this.setModInfo("Auto eat Filet O' Fortune.");
    this.except();
  }

  public static AutoFilet getINSTANCE() {
    if (INSTANCE == null) {
      Iterator<Module> var2 = ModuleManager.modules.iterator();

      Module m;
      do {
        if (!var2.hasNext()) {
          return null;
        }

        m = var2.next();
      } while (m.getClass() != AutoFilet.class);

      INSTANCE = (AutoFilet) m;
    }
    return INSTANCE;
  }

  @EventHandler
  public void tick(EventTick event) {
    if (tick >= 50) {
      SkyblockArea area = new SkyblockArea();
      area.updateCurrentArea();
      if (!area.isIn(Areas.NULL)) {
        // 在skyblock里面

        if (this.ticksTimer.hasReached(1000) && isDoingMacro() && this.coolDownTimer.hasReached(60_000)) {
          String footer = ((GuiPlayerTabAccessor) this.mc.ingameGUI.getTabList()).getFooter().getFormattedText();

          if (!footer.contains("Filet O' Fortune")) {
            Helper.sendMessage("Found Filet O' Fortune Buff Expired! Ready to eat fish!");
            // 要吃鱼了
            disableMacros();
            fishTick = 0;
          }

          this.ticksTimer.reset();
        }

        // 吃鱼核心逻辑
        if (fishTick == 10) {
          for (int i = 0; i < 9; ++i) {
            ItemStack item = this.mc.thePlayer.inventory.getStackInSlot(i);
            if (item != null && ItemUtils.getSkyBlockID(item).contains("FILET_O_FORTUNE")) {
              prevItem = this.mc.thePlayer.inventory.currentItem;
              this.mc.thePlayer.inventory.currentItem = i;
              return;
            }
          }
          Helper.sendMessage("Cannot find Filet O' Fortune to eat. continue to mine (>_<)");
          reEnableMacros();
          coolDownTimer.reset();
          fishTick = -1;
        }
        if (fishTick == 20) {
          this.mc.playerController.sendUseItem(
              this.mc.thePlayer, this.mc.theWorld,
              this.mc.thePlayer.inventory.getStackInSlot(this.mc.thePlayer.inventory.currentItem)
          );
        }
        if (fishTick == 30) {
          this.mc.thePlayer.inventory.currentItem = prevItem;
          reEnableMacros();
          coolDownTimer.reset();
          fishTick = -1;
        }
      }

      if (fishTick != -1) {
        fishTick++;
      }
    }
    tick++;
  }

  private void disableMacros() {
    for (Module mod : ModuleManager.getModulesInType(ModuleType.Mining)) {
      if (mod != this && mod.isEnabled() && !mod.excepted) {
        mod.setEnabled(false);
        this.mods.add(mod);
      }
    }
  }

  @SubscribeEvent
  public void clear(WorldEvent.Load event) {
    tick = 0;
  }

  private boolean isDoingMacro() {
    for (Module mod : ModuleManager.getModulesInType(ModuleType.Mining)) {
      if (mod != this && mod.isEnabled() && !mod.excepted && !Objects.equals(mod.getName(), "PinglessMining")) {
        if (Objects.equals(mod.getName(), "AutoGemstone")) {
          return AutoRuby.getINSTANCE().started;
        }
        return true;
      }
    }
    return false;
  }

  private void reEnableMacros() {

    for (Module mod : this.mods) {
      mod.setEnabled(true);
    }

    this.mods.clear();
  }

  @Override
  public void onEnable() {
    super.onEnable();
    tick = 0;
  }
}
