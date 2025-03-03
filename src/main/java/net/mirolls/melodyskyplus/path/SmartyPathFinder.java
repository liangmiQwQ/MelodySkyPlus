package net.mirolls.melodyskyplus.path;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class SmartyPathFinder {
  private Minecraft mc = null;

  public SmartyPathFinder() {
    mc = Minecraft.getMinecraft();
  }


  private double getPenalty(PathNode pathNode) {
    double cost = 0.0D;
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        Block block = mc.theWorld.getBlockState(pathNode.pos.add(i, 1, j)).getBlock();
        if (block != Blocks.air && block != Blocks.snow_layer && block != Blocks.snow) {
          cost += 2.0D;
        }
      }
    }
    return cost;
  }

  public Boolean isWalkable(BlockPos pos) {
    Block dataBlock = mc.theWorld.getBlockState(pos).getBlock();
    if (dataBlock.getMaterial().isLiquid() || (!dataBlock.getMaterial().isSolid() && Block.getIdFromBlock(dataBlock) != 78)) {
      return Boolean.FALSE;
    }
    double totalHeight = 0.0D;
    Block block1 = mc.theWorld.getBlockState(pos.add(0, 1, 0)).getBlock();
    Block block2 = mc.theWorld.getBlockState(pos.add(0, 2, 0)).getBlock();
    Block block3 = mc.theWorld.getBlockState(pos.add(0, 3, 0)).getBlock();
    if (block1 != Blocks.air) {
      totalHeight = totalHeight + block1.getBlockBoundsMaxY();
    }
    if (block2 != Blocks.air) {
      totalHeight = totalHeight + block2.getBlockBoundsMaxY();
    }
    if (block3 != Blocks.air) {
      totalHeight = totalHeight + block3.getBlockBoundsMaxY();
    }
    return totalHeight < 0.6D;
  }
}
