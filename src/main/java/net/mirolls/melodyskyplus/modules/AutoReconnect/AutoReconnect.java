package net.mirolls.melodyskyplus.modules.AutoReconnect;

import xyz.Melody.Event.value.Numbers;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;

import java.util.Iterator;

public class AutoReconnect extends Module {

  private static AutoReconnect INSTANCE;
  public String host;
  public Numbers<Double> delay = new Numbers<>("Delay", 20.0, 0.0, 120.0, 1.0);
  public int second;

  public AutoReconnect() {
    super("AutoReconnect", ModuleType.QOL);
    this.setModInfo("Auto reconnect.");
    this.addValues(delay);
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

  public void reconnect(SetStringCallback callbackSetString, ReconnectCallback callbackReconnect) {
    second = delay.getValue().intValue();
    oneSecond(callbackSetString, callbackReconnect);
  }

  private void oneSecond(SetStringCallback callbackSetString, ReconnectCallback callbackReconnect) {
    new Thread(() -> {
      try {
        if (second > 0) {
          Thread.sleep(1000);
          second -= 1;
          oneSecond(callbackSetString, callbackReconnect);
          callbackSetString.call(String.valueOf(second));
        } else {
          callbackReconnect.call();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
  }
}
