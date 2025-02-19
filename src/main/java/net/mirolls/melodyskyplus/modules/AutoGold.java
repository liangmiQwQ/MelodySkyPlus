package net.mirolls.melodyskyplus.modules;

import net.minecraft.client.settings.KeyBinding;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;

import java.util.Iterator;

public class AutoGold extends Module {
  private static AutoGold INSTANCE;
  private final TimerUtil walkTimer;
  private Numbers<Double> walkTime = new Numbers<>("WalkTime(s)", 4.0, 1.0, 10.0, 0.5);


  public AutoGold() {
    super("AutoGold", new String[]{""}, ModuleType.Mining);

    walkTimer = (new TimerUtil()).reset();

    this.setModInfo("Auto mine gold and find path in Mines of Divan");
  }

  public static AutoGold getINSTANCE() {
    if (INSTANCE == null) {
      Iterator<Module> var2 = ModuleManager.modules.iterator();

      Module m;
      do {
        if (!var2.hasNext()) {
          return null;
        }

        m = var2.next();
      } while (m.getClass() != AutoGold.class);

      INSTANCE = (AutoGold) m;
    }
    return INSTANCE;
  }

  @EventHandler
  private void onTick(EventTick event) {
    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);

    if (walkTimer.hasReached(1000 - (walkTime.getValue() * 1000)/* 走路的时间 */)) {
      KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), true);
    }
    if (walkTimer.hasReached(1000)) {
      walkTimer.reset();
      KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), false);
    }
  }

  @Override
  public void onDisable() {
    if (ModuleManager.getModuleByName("GoldNuker").isEnabled()) {
      ModuleManager.getModuleByName("GoldNuker").setEnabled(false);
    }
    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
  }

  @Override
  public void onEnable() {
    if (!ModuleManager.getModuleByName("GoldNuker").isEnabled()) {
      ModuleManager.getModuleByName("GoldNuker").setEnabled(true);
    }
    walkTimer.reset();
  }
}
