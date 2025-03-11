package net.mirolls.melodyskyplus.modules;

import net.mirolls.melodyskyplus.client.AntiBug;
import xyz.Melody.module.Module;

import java.util.ArrayList;
import java.util.List;

public class MelodyPlusModules {
  public static List<Module> newModules() {
    List<Module> newModules = new ArrayList<>();

    if (AntiBug.isBugRemoved()) {
      newModules.add(new Failsafe());
      newModules.add(new AutoReconnect());
      newModules.add(new AutoFilet());
      newModules.add(new AutoGold());
    }

    return newModules;
  }
}
