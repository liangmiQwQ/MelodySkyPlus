package net.mirolls.melodyskyplus.client.cmd;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.System.Commands.Command;
import xyz.Melody.Utils.Helper;

public class RayTraceCommand extends Command {
  public RayTraceCommand() {
    super(".raytrace", new String[]{"rt"}, "", "sketit");
  }


  @Override
  public String execute(String[] args) {
    BlockPos targetBP = new BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));

    Helper.sendMessage(PlayerUtils.rayTrace(targetBP));

    return null;
  }

}
