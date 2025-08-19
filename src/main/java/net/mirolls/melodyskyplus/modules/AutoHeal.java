package net.mirolls.melodyskyplus.modules;

import net.mirolls.melodyskyplus.client.ModulePlus;
import xyz.Melody.module.ModuleType;

public class AutoHeal extends ModulePlus {
  public AutoHeal() {
    super("AutoHeat", ModuleType.QOL);
    this.setModInfo("Auto use heal item when you low");
    this.except();
  }
}
