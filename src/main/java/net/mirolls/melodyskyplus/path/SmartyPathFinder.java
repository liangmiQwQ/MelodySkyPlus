package net.mirolls.melodyskyplus.path;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmartyPathFinder {
  private final List<BlockPos> specialUnbreakableBlocks = new ArrayList<>();
  private final List<PathNode> testedBlocks = new ArrayList<>();
  private final List<PathNode> closedBlocks = new ArrayList<>();

  private final Minecraft mc;


  public SmartyPathFinder() {
    mc = Minecraft.getMinecraft();
  }

  public List<BlockPos> findPath(BlockPos target) {
    BlockPos posPlayer = mc.thePlayer.getPosition();
    PathNode root = new PathNode(0, distance(posPlayer, target), null, posPlayer);

    while (true) {
    }
  }


  /**
   * 作用就是找到一个点周围的方块 然后添加到数组里头
   *
   * @param parent 父节点
   * @param target 终点
   */
  private void openBlock(PathNode parent, BlockPos target) {
    if (!closedBlocks.contains(parent)) {
      closedBlocks.add(parent); // 关闭

      for (int i = 0; i < 6; i++) {
        BlockPos pos;
        if (i == 0) {
          pos = parent.pos.add(1, 0, 0);
        } else if (i == 1) {
          pos = parent.pos.add(0, 1, 0);
        } else if (i == 2) {
          pos = parent.pos.add(0, 0, 1);
        } else if (i == 3) {
          pos = parent.pos.add(-1, 0, 0);
        } else if (i == 4) {
          pos = parent.pos.add(0, -1, 0);
        } else {
          pos = parent.pos.add(0, 0, -1);
        }

        boolean posChecked = false;
        for (PathNode node : testedBlocks) {
          if (node.pos.getX() == pos.getX() && node.pos.getY() == pos.getY() && node.pos.getZ() == pos.getZ()) {
            posChecked = true;
            break;
          }
        }

        if (!posChecked) {
          if (isWalkable(pos)) {
            new PathNode(1, distance(pos, target), parent, pos);
          } else {
            if (isBreakable(pos)) {
              new PathNode(1, distance(pos, target), parent, pos);
            }
          }
        }
      }
    }
  }

  private int distance(BlockPos pos1, BlockPos pos2) {
    return (pos1.getX() - pos2.getX()) + (pos1.getY() - pos2.getY()) + (pos1.getZ() - pos2.getZ());
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
      return false;
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

  public Boolean isBreakable(BlockPos pos) {
    Block footBlock = mc.theWorld.getBlockState(pos).getBlock();
    Block headBlock = mc.theWorld.getBlockState(pos).getBlock();

    List<Block> unbreakableBlocks = Arrays.asList(
        Blocks.wool, Blocks.sand, Blocks.gravel, Blocks.rail,
        Blocks.bedrock
    );

    boolean footBlockBreakable = false;
    boolean headBlockBreakable = false;

    if (unbreakableBlocks.contains(footBlock)) {
      footBlockBreakable = !specialUnbreakableBlocks.contains(pos);
    }

    if (unbreakableBlocks.contains(headBlock)) {
      headBlockBreakable = !specialUnbreakableBlocks.contains(pos);
    }

    return footBlockBreakable && headBlockBreakable;
  }

}
