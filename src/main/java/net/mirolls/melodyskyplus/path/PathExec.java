package net.mirolls.melodyskyplus.path;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class PathExec {
  public final List<PathPos> importantNodes = new ArrayList<>();

  public void go(List<PathPos> path) {
    // Minecraft mc = Minecraft.getMinecraft();

    importantNodes.add(path.get(0));
    PathPos lastPathPos = null;

    for (PathPos pos : path) {
      if (pos.getType() == PathNodeType.WALK) {
        if (canGo(importantNodes.get(importantNodes.size() - 1).getPos(), pos.getPos())) {
          // 可以直接的走过去
          lastPathPos = pos;
        } else {
          if (lastPathPos != null) {
            importantNodes.add(lastPathPos);
            lastPathPos = null;
          }
        }
      } else {
        if (lastPathPos != null) {
          // 把最后的一个点添进去
          importantNodes.add(lastPathPos);
          lastPathPos = null;
        }
        importantNodes.add(pos); // 其他的是全要的
      }
    }
  }

  private boolean canGo(BlockPos startPos, BlockPos target) {
    Minecraft mc = Minecraft.getMinecraft();


    int yPos = startPos.getY();
    for (BlockPos pos : getBlocksBetween(startPos, target)) {
      BlockPos bp = new BlockPos(pos.getX(), yPos, pos.getZ());
      IBlockState blockState = mc.theWorld.getBlockState(bp);
      if (blockState.getBlock() != Blocks.air) {
        // 如果不是空气的
        if (blockState.getBlock().getRegistryName().contains("slab") && blockState.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM) {
          // 如果是下半砖的
          yPos += 1; // 提升一下y的value 依然是个好汉
          bp = new BlockPos(pos.getX(), yPos, pos.getZ());
        } else {
          // 如果遇到墙了
          return false;
        }
      }

      // 检测脚底下能不能走的
      if (!mc.theWorld.getBlockState(bp.down()).getBlock().getMaterial().isSolid()) {
        return false;
      }
    }
    return true;
  }

  private List<BlockPos> getBlocksBetween(BlockPos start, BlockPos end) {
    List<BlockPos> blocks = new ArrayList<>();

    int x0 = start.getX();
    int z0 = start.getZ();
    int x1 = end.getX();
    int z1 = end.getZ();

    int dx = Math.abs(x1 - x0);
    int dz = Math.abs(z1 - z0);

    int sx = x0 < x1 ? 1 : -1;
    int sz = z0 < z1 ? 1 : -1;

    int err = dx - dz;
    int e2;

    while (true) {
      blocks.add(new BlockPos(x0, start.getY(), z0));

      if (x0 == x1 && z0 == z1) {
        break;
      }

      e2 = 2 * err;

      if (e2 > -dz) {
        err -= dz;
        x0 += sx;
      }

      if (e2 < dx) {
        err += dx;
        z0 += sz;
      }
    }

    return blocks;
  }


}
