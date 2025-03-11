package net.mirolls.melodyskyplus.modules;

import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;

import java.util.Iterator;

public class AutoReconnect extends Module {

  private static AutoReconnect INSTANCE;
  public String host;

  public AutoReconnect() {
    super("AutoReconnect", ModuleType.QOL);
    this.setModInfo("Auto reconnect.");
    this.except();
  }

  public static AutoReconnect getInstance() {
    if (INSTANCE == null) {
      Iterator<Module> var2 = ModuleManager.modules.iterator();

      Module m;
      do {
        if (!var2.hasNext()) {
          return null;
        }

        m = var2.next();
      } while (m.getClass() != AutoReconnect.class);

      INSTANCE = (AutoReconnect) m;
    }
    return INSTANCE;
  }
}
