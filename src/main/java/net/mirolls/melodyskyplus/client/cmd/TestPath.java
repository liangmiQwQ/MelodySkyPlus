package net.mirolls.melodyskyplus.client.cmd;

import net.mirolls.melodyskyplus.path.test.TryTest;
import xyz.Melody.System.Commands.Command;
import xyz.Melody.Utils.Helper;

public class TestPath extends Command {
  public TestPath() {
    super(".testp", new String[]{"testp", "te"}, "", "sketit");
  }

  @Override
  public String execute(String[] strings) {
    TryTest test = new TryTest();
    Helper.sendMessage("Start to test");
    test.tick = 0;


    return null;
  }
}
