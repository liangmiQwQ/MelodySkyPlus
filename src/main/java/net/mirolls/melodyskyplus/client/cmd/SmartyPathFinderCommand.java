package net.mirolls.melodyskyplus.client.cmd;

import java.util.Objects;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.modules.SmartyPathFinder;
import xyz.Melody.System.Commands.Command;
import xyz.Melody.Utils.Helper;

public class SmartyPathFinderCommand extends Command {
  public SmartyPathFinderCommand() {
    super(".smartypathfinder", new String[] {"spf", "sgoto", "pf", "sgo"}, "", "sketit");
  }

  @Override
  public String execute(String[] args) {
    SmartyPathFinder smartyPathFinder = Objects.requireNonNull(SmartyPathFinder.getINSTANCE());

    if (args.length == 3) {
      // 寻路命令系统
      Helper.sendMessage("Start to find path");

      long startTime = System.currentTimeMillis();

      try {
        BlockPos targetBP =
            new BlockPos(
                Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        smartyPathFinder.strongClear(true);
        smartyPathFinder.go(targetBP);
      } catch (IllegalStateException e) {
        Helper.sendMessage("Sorry, Cant find path.");
      }

      long finishTime = System.currentTimeMillis();
      Helper.sendMessage("Finish path finding in " + (finishTime - startTime) + "ms");
    } else if (args.length == 1) {
      if (args[0].toLowerCase().contains("clear")) {
        Helper.sendMessage("Successfully cleared");
        smartyPathFinder.strongClear(false);
      } else {
        printUsage();
      }
    } else {
      printUsage();
    }

    return null;
  }

  private void printUsage() {
    Helper.sendMessageWithoutPrefix("==================== SmartyPathFinder ====================");
    Helper.sendMessageWithoutPrefix("Edit config in module SmartyPathFinder.");
    Helper.sendMessageWithoutPrefix(
        "Currently unstable and under developing. Might cause some errors.");
    Helper.sendMessageWithoutPrefix("");
    Helper.sendMessageWithoutPrefix(".spf [x] [y] [z] - Go to a block pos.");
    Helper.sendMessageWithoutPrefix(".spf clear - Stop finding path and clear renderer.");
    Helper.sendMessageWithoutPrefix(".spf help - Show information of SmartyPathFinder");
  }
}
