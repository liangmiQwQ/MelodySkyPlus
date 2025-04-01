package net.mirolls.melodyskyplus.path;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.path.find.AStarPathFinder;
import net.mirolls.melodyskyplus.path.find.PathPos;
import net.mirolls.melodyskyplus.path.optimization.PathOptimizer;
import net.mirolls.melodyskyplus.path.type.Node;

import java.util.List;

public class SmartyPathFinder {
  // 汇总 执行所有操作

  private final Minecraft mc = Minecraft.getMinecraft();
  public List<PathPos> aStarPath;
  public List<Node> path;

  public void go(BlockPos end) {
    int x = (int) (mc.thePlayer.posX - mc.thePlayer.posX % 1 - 1);
    int y = (int) (mc.thePlayer.posY - mc.thePlayer.posY % 1);
    int z = (int) (mc.thePlayer.posZ - mc.thePlayer.posZ % 1 - 1);
    BlockPos start = new BlockPos(x, y, z); // Minecraft提供的.getPosition不好用 返回的位置经常有较大的误差 这样是最保险的

    go(start, end);
  }

  public void go(BlockPos start, BlockPos end) {
    aStarPath = new AStarPathFinder().findPath(start, end);
    path = new PathOptimizer().optimize(aStarPath);

    if (aStarPath == null || path == null) {
      throw new IllegalStateException("Path no Found");
    }
  }

  public void clear() {
    aStarPath.clear();
    path.clear();
  }
}
