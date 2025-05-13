package net.mirolls.melodyskyplus.client.cmd;

import net.mirolls.melodyskyplus.modules.AutoHollow;
import xyz.Melody.System.Commands.Command;

import java.util.Objects;

public class AutoHollowCommand extends Command {
  public AutoHollowCommand() {
    super(".autohollow", new String[]{"ah"}, "", "sketit");
  }

  @Override
  public String execute(String[] args) {
    AutoHollow autoHollow = Objects.requireNonNull(AutoHollow.getINSTANCE());

    if (args.length >= 1) {
      if (args[0].toLowerCase().contains("start")) {
        autoHollow.start();
      } else if (args[0].toLowerCase().contains("stop")) {
        autoHollow.clear();
      }
    }
    return null;

  }
}
