package net.mirolls.melodyskyplus.path;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SmartyPathFinder {
  private Minecraft mc = null;

  private List<BlockPos> unMineableBlocks = new ArrayList<>();

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
    Block block = mc.theWorld.getBlockState(pos).getBlock();
    if (block.getMaterial().isLiquid() || (!block.getMaterial().isSolid() && Block.getIdFromBlock(block) != 78)) {
      return Boolean.FALSE;
    }
    double totalHeight = 0.0D;
    Block blockHead = mc.theWorld.getBlockState(pos.add(0, 1, 0)).getBlock();
    Block blockTop = mc.theWorld.getBlockState(pos.add(0, 2, 0)).getBlock();
    Block blockHigh = mc.theWorld.getBlockState(pos.add(0, 3, 0)).getBlock();
    if (blockHead != Blocks.air) {
      totalHeight = totalHeight + blockHead.getBlockBoundsMaxY();
    }
    if (blockTop != Blocks.air) {
      totalHeight = totalHeight + blockTop.getBlockBoundsMaxY();
    }
    if (blockHigh != Blocks.air) {
      totalHeight = totalHeight + blockHigh.getBlockBoundsMaxY();
    }
    return totalHeight < 0.6D;
  }

  public Boolean isMineable(BlockPos pos) {
    Block block = mc.theWorld.getBlockState(pos).getBlock();
    if (
        block != Blocks.cobblestone_wall
            && block != Blocks.wool
            && block != Blocks.sand
            && block != Blocks.gravel
            && block != Blocks.rail
    ) {
      return !unMineableBlocks.contains(pos);
    }
    
    return false;
  }

}
