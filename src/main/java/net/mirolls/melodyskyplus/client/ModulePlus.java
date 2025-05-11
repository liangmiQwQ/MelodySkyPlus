package net.mirolls.melodyskyplus.client;

import xyz.Melody.Utils.Helper;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;

public abstract class ModulePlus extends Module {
  public ModulePlus(String name, String[] alias, ModuleType type) {
    super(name, alias, type);
  }

  public ModulePlus(String name, ModuleType type) {
    super(name, type);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    if (AntiBug.isBugRemoved()) {
      Helper.sendMessage("Can't auth your account");
      this.setEnabled(false);
    }
  }
}
