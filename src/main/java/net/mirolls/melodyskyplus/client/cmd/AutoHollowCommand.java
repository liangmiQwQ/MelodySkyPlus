package net.mirolls.melodyskyplus.client.cmd;

import java.util.Objects;
import net.mirolls.melodyskyplus.modules.AutoHollow;
import xyz.Melody.System.Commands.Command;
import xyz.Melody.Utils.Helper;

public class AutoHollowCommand extends Command {
  public AutoHollowCommand() {
    super(".autohollow", new String[] {"ah"}, "", "sketit");
  }

  @Override
  public String execute(String[] args) {
    AutoHollow autoHollow = Objects.requireNonNull(AutoHollow.getINSTANCE());

    if (args.length >= 1) {
      if (args[0].toLowerCase().contains("start")) {
        autoHollow.start();
      } else if (args[0].toLowerCase().contains("stop")) {
        Helper.sendMessage("AutoHollow: Stopped.");
        autoHollow.clear();
      }
    }
    return null;
  }
}
