package net.mirolls.melodyskyplus.client.cmd;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.path.optimization.PathOptimizer;
import xyz.Melody.System.Commands.Command;
import xyz.Melody.Utils.Helper;

public class CanGo extends Command {
  public CanGo() {
    super(".cango", new String[]{"cg"}, "", "sketit");
  }

  @Override
  public String execute(String[] args) {
    if (args.length == 6) {

      PathOptimizer exec = new PathOptimizer();

      BlockPos startBP = new BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
      BlockPos endBP = new BlockPos(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));


      Helper.sendMessage("CanGo: " + exec.canGo(startBP, endBP));


      MelodySkyPlus.canGoRenderer.startRender(exec.routeVec);
    } else {
      Helper.sendMessage("Bad command");
    }

    return null;
  }
}
