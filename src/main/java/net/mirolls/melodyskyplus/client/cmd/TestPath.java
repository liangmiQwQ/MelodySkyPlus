package net.mirolls.melodyskyplus.client.cmd;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.path.PathOptimizer;
import net.mirolls.melodyskyplus.path.test.CanGoRenderer;
import xyz.Melody.System.Commands.Command;
import xyz.Melody.Utils.Helper;

public class TestPath extends Command {
  public TestPath() {
    super(".testp", new String[]{"testp", "te"}, "", "sketit");
  }

  @Override
  public String execute(String[] strings) {
    PathOptimizer exec = new PathOptimizer();

    Helper.sendMessage("CanGo: " + exec.canGo(new BlockPos(-372, 4, -1244),
        new BlockPos(-370, 4, -1256)));


    new CanGoRenderer().startRender(exec.routeVec);
    return null;
  }
}
