package net.mirolls.melodyskyplus.modules;

import net.mirolls.melodyskyplus.Verify;
import net.mirolls.melodyskyplus.modules.AutoReconnect.AutoReconnect;
import xyz.Melody.module.Module;

import java.util.ArrayList;
import java.util.List;

public class MelodyPlusModules {
  public static List<Module> newModules() {
    List<Module> newModules = new ArrayList<>();

    if (Verify.isVerified()) {
      newModules.add(new SmartyPathFinder());
      newModules.add(new Failsafe());
      newModules.add(new AutoReconnect());
      newModules.add(new AutoFilet());
      newModules.add(new AutoGold());
      newModules.add(new AutoLobby());
      newModules.add(new AutoHollow());
      newModules.add(new AutoHeat());
      newModules.add(new AutoHeal());
    }

    return newModules;
  }
}
