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
  // 一个BlockPos有3个状态
  /* 1. 没有被扫描过 toOpen
     2. 被扫描过了 opened
     3. 被扫描过了 并且以他为中心开展了新的节点 closed
     4. `特殊状态` cantOpen 无法打开的方块 避免重复多次尝试打开这个方块 */
  private final List<PathNode> openedBlocks = new ArrayList<>();
  private final List<PathNode> closedBlocks = new ArrayList<>();
  private final List<BlockPos> cantOpenBlocks = new ArrayList<>();


  private final Minecraft mc;


  public SmartyPathFinder() {
    mc = Minecraft.getMinecraft();
  }

  public List<BlockPos> findPath(BlockPos target) {
    BlockPos posPlayer = mc.thePlayer.getPosition();
    PathNode root = new PathNode(0, distance(posPlayer, target), null, posPlayer);

    if (posPlayer == target) {
      return new ArrayList<>();
      // 如果开局即巅峰 则直接返回
    }
    openedBlocks.add(root);

    do {
      PathNode nodeToClose = null;
      for (PathNode openedBlock : openedBlocks) {
        if (nodeToClose == null) {
          nodeToClose = openedBlock;
        } else {
          if (Math.abs(openedBlock.fCost() - nodeToClose.fCost()) < 0.1D) {
            double mathDistanceToTargetOpenedBlocks = Math.hypot(Math.hypot(openedBlock.pos.getX(), openedBlock.pos.getY()), openedBlock.pos.getZ());
            double mathDistanceToTargetNodeToClose = Math.hypot(Math.hypot(nodeToClose.pos.getX(), nodeToClose.pos.getY()), nodeToClose.pos.getZ());

            if (mathDistanceToTargetOpenedBlocks < mathDistanceToTargetNodeToClose) {
              // 如果说还是opened更小 那还是进行一个替换比较好 (这是直线距离 不采用曼哈顿距离)
              nodeToClose = openedBlock;
            }
          } else if (openedBlock.fCost() < nodeToClose.fCost()) {
            // 如果这个东西比现在的玩意更实惠 我们选更好的
            nodeToClose = openedBlock;
          }
        }
      }

      PathNode block = closeBlock(nodeToClose, target);

      if (block != null) {
        break;
      }
    } while (true);

    return new ArrayList<>(); // TODO: 增加返回 优化节点 剔除冗余等
  }


  /**
   * 作用就是找到一个点周围的方块 然后添加到数组里头
   *
   * @param parent 父节点
   * @param target 终点
   * @return 如果返回 则代表到达了终点
   */
  private PathNode closeBlock(PathNode parent, BlockPos target) {
    if (openedBlocks.contains(parent)) {
      closedBlocks.add(parent); // 关闭
      openedBlocks.remove(parent);

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

        boolean posChecked = isPosChecked(pos);

        if (!posChecked) {
          if (isWalkable(pos)) {
            int distance = distance(pos, target);
            PathNode node = new PathNode(parent.gCost + 1 + getPenalty(pos), distance, parent, pos);
            openedBlocks.add(node);
            if (distance == 0) {
              return node;
            }
          } else {
            if (isBreakable(pos)) {
              int distance = distance(pos, target);
              PathNode node = new PathNode(parent.gCost + 1 + getPenalty(pos), distance * 1.5 /* 挖掘可能会让效率略微减少2/3左右*/, parent, pos);
              openedBlocks.add(node);
              if (distance == 0) {
                return node;
              }
            }
          }

          cantOpenBlocks.add(pos);
        }
      }
    }
    return null;
  }

  private boolean isPosChecked(BlockPos pos) {
    boolean posChecked = false;
    for (PathNode node : closedBlocks) {
      if (node.pos.getX() == pos.getX() && node.pos.getY() == pos.getY() && node.pos.getZ() == pos.getZ()) {
        posChecked = true;
        break;
      }
    }
    for (PathNode node : openedBlocks) {
      if (node.pos.getX() == pos.getX() && node.pos.getY() == pos.getY() && node.pos.getZ() == pos.getZ()) {
        posChecked = true;
        break;
      }
    }
    if (cantOpenBlocks.contains(pos)) {
      posChecked = true;
    }
    return posChecked;
  }

  private int distance(BlockPos pos1, BlockPos pos2) {
    return (pos1.getX() - pos2.getX()) + (pos1.getY() - pos2.getY()) + (pos1.getZ() - pos2.getZ());
  }

  private double getPenalty(BlockPos pos) {
    double cost = 0.0D;
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        Block block = mc.theWorld.getBlockState(pos.add(i, 1, j)).getBlock();
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
