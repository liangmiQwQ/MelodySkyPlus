package net.mirolls.melodyskyplus.modules;

import net.mirolls.melodyskyplus.client.ModulePlus;
import xyz.Melody.Client;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;

import java.util.ArrayList;
import java.util.List;

public class AutoHeal extends ModulePlus {
  private final Numbers<Double> limit = new Numbers<>("Limit(%)", 30.0, 1.0, 99.0, 1.0);
  private final Numbers<Double> slot = new Numbers<>("Heal Item Slot", 8.0, 1.0, 8.0, 1.0);
  HealStatus healStatus = HealStatus.getCleanStatus();
  List<Module> mods = new ArrayList<>();

  public AutoHeal() {
    super("AutoHeal", ModuleType.QOL);
    this.setModInfo("Auto use heal item when you low");
    this.addValues(limit, slot);
    this.except();
  }

  @EventHandler
  public void onTick(EventTick tick) {
    if (healStatus.healing) {
      // 执行治疗
      performHealAction();
      return;
    }

    float health = mc.thePlayer.getHealth();
    float maxHealth = mc.thePlayer.getMaxHealth();
    float healthPercentage = (health / maxHealth) * 100;

    if (healthPercentage <= limit.getValue()) {
      // 玩家生命值低于阈值，治疗
      performHealAction();
    }
  }

  private void performHealAction() {
    // 进行治疗
    healStatus.tick++;
    healStatus.healing = true;

    if (healStatus.tick == 0) {
      disableMacros();
    }
    if (healStatus.tick == 5) {
      mc.thePlayer.inventory.currentItem = slot.getValue().intValue() - 1;
    }
    if (healStatus.tick >= 10 && healStatus.tick % 4 == 0 && healStatus.tick < 30) {
      Client.rightClick();
    }
    if (healStatus.tick == 30) {
      reEnableMacros();
      healStatus = HealStatus.getCleanStatus();
    }
  }

  private void disableMacros() {
    List<Module> allMods = ModuleManager.getModulesInType(ModuleType.Mining);
    allMods.addAll(ModuleManager.getModulesInType(ModuleType.Fishing));

    for (Module mod : allMods) {
      if (mod != this && mod.isEnabled() && !mod.excepted) {
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

  public void onDisable() {
    this.mods.clear();
    healStatus = HealStatus.getCleanStatus();
    super.onDisable();
  }

  public void onEnable() {
    super.onEnable();
  }

}

class HealStatus {
  int tick;
  boolean healing;

  static HealStatus getCleanStatus() {
    HealStatus instance = new HealStatus();
    instance.tick = -1;
    instance.healing = false;
    return instance;
  }
}
