package net.mirolls.melodyskyplus.client.cmd;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.modules.SmartyPathFinder;
import xyz.Melody.System.Commands.Command;
import xyz.Melody.Utils.Helper;

import java.util.Objects;

public class CrystalHollowGoTo extends Command {
  public CrystalHollowGoTo() {
    super(".crystalhollowgoto", new String[]{"crystalgoto", "chgoto", "cgoto", "cgo"}, "", "sketit");
  }

  @Override
  public String execute(String[] args) {
    SmartyPathFinder smartyPathFinder = Objects.requireNonNull(SmartyPathFinder.getINSTANCE());

    if (args.length == 3) {
      // 寻路命令系统
      Helper.sendMessage("Start to find path");

      long startTime = System.currentTimeMillis();

      try {
        BlockPos targetBP = new BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        smartyPathFinder.go(targetBP);
      } catch (IllegalStateException e) {
        Helper.sendMessage("Sorry, Cant find path.");
      }

      long finishTime = System.currentTimeMillis();
      Helper.sendMessage("Finish path finding in " + (finishTime - startTime) + "ms");
    } else if (args.length == 4) {
      // 寻路命令系统
      Helper.sendMessage("Start to find path without break ability and jumpBoost");

      long startTime = System.currentTimeMillis();

      try {
        BlockPos targetBP = new BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        smartyPathFinder.go(targetBP);
      } catch (IllegalStateException e) {
        Helper.sendMessage("Sorry, Cant find path.");
      }

      long finishTime = System.currentTimeMillis();
      Helper.sendMessage("Finish path finding in " + (finishTime - startTime) + "ms");
    } else if (args.length == 1) {
      Helper.sendMessage("Renderer cleared");

      smartyPathFinder.clear();
    }
    return null;
  }
}
