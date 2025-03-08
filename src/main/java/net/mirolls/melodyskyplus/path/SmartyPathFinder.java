package net.mirolls.melodyskyplus.path;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.*;

public class SmartyPathFinder {
  private static final BlockPos[] OFFSETS = {
      new BlockPos(1, 0, 0),  // 右
      new BlockPos(-1, 0, 0), // 左
      new BlockPos(0, 1, 0),  // 上
      new BlockPos(0, -1, 0), // 下
      new BlockPos(0, 0, 1),  // 前
      new BlockPos(0, 0, -1)  // 后
  };
  private final Set<BlockPos> specialUnbreakableBlocks;
  // 一个BlockPos有3个状态
  /* 1. 没有被扫描过 toOpen
     2. 被扫描过了 opened
     3. 被扫描过了 并且以他为中心开展了新的节点 closed */

  //  private final Set<PathNode> openedBlocks = new HashSet<>();
  private final PriorityQueue<PathNode> openedBlocks = new PriorityQueue<>(Comparator.comparingDouble(PathNode::fCost));

  private final Set<BlockPos> visitedPositions = new HashSet<>();
  private final Map<BlockPos, IBlockState> blockStateMap = new HashMap<>();
  private final Set<PathNode> closedBlocks = new HashSet<>();
  private final boolean canMineBlocks;
  private final Minecraft mc;


  public SmartyPathFinder(boolean canMineBlocks, Set<BlockPos> specialUnbreakableBlocks) {
    mc = Minecraft.getMinecraft();
    this.canMineBlocks = canMineBlocks;
    this.specialUnbreakableBlocks = specialUnbreakableBlocks;
  }

  public SmartyPathFinder(boolean canMineBlocks) {
    this(canMineBlocks, new HashSet<>());
  }

  public SmartyPathFinder() {
    this(true);
  }

  public List<PathPos> findPath(BlockPos target) {
    BlockPos posPlayer = mc.thePlayer.getPosition();
    PathNode root = new PathNode(0, distance(posPlayer, target), null, posPlayer, PathNodeType.WALK);

    if (posPlayer == target) {
      return new ArrayList<>();
      // 如果开局即巅峰 则直接返回
    }
    openedBlocks.add(root);
    visitedPositions.add(root.pos);

    PathNode targetPathNode;
    do {
      PathNode nodeToClose = openedBlocks.poll();
      /*for (PathNode openedBlock : openedBlocks) {
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
      }*/

      PathNode block = closeBlock(nodeToClose, target);

      if (block != null) {
        targetPathNode = block;
        break;
      }
    } while (true);


    return buildPath(targetPathNode); // TODO: 增加返回 优化节点 剔除冗余等
  }

  private List<PathPos> buildPath(PathNode endNode) {
    LinkedList<PathNode> paths = new LinkedList<>();
    List<PathPos> returnPaths = new ArrayList<>();

    paths.add(endNode);
    if (endNode == null) return null;

    while (true) {
      PathNode node = paths.get(0);
      if (node.nodeParent == null) break;
      paths.addFirst(node.nodeParent);
    }

    for (PathNode node : paths) {
      returnPaths.add(new PathPos(node.type, node.pos));
    }

    return returnPaths;
  }


  /**
   * 作用就是找到一个点周围的方块 然后添加到数组里头
   *
   * @param parent 父节点
   * @param target 终点
   * @return 如果返回 则代表到达了终点
   */
  private PathNode closeBlock(PathNode parent, BlockPos target) {
    if (visitedPositions.contains(parent.pos)) {
      closedBlocks.add(parent); // 关闭
      openedBlocks.remove(parent);

      for (BlockPos offset : OFFSETS) {
        BlockPos pos = parent.pos.add(offset);

        boolean posChecked = isPosChecked(pos);

        if (!posChecked) {
          int walkType = getWalkType(pos);
          if (walkType == 0) {
            int distance = distance(pos, target);
            PathNode node = new PathNode(parent.gCost + 1 + getPenalty(pos), distance, parent, pos, PathNodeType.WALK);
            openedBlocks.add(node);
            visitedPositions.add(node.pos);
            if (distance == 0) {
              return node;
            }
          } else if (walkType == 1) {
            int distance = distance(pos, target);
            PathNode node = new PathNode(parent.gCost + 1 + 3, distance, parent, pos, PathNodeType.ABILITY);
            openedBlocks.add(node);
            visitedPositions.add(node.pos);
            if (distance == 0) {
              return node;
            }
          } else {
            if (isBreakable(pos) && parent.type != PathNodeType.ABILITY) {
              int distance = distance(pos, target);
              PathNode node = new PathNode(parent.gCost + 1 + 4, distance, parent, pos, PathNodeType.MINE);
              openedBlocks.add(node);
              visitedPositions.add(node.pos);
              if (distance == 0) {
                return node;
              }
            }
          }

        }
      }
    }
    return null;
  }

  private boolean isPosChecked(BlockPos pos) {
    return visitedPositions.contains(pos);
  }

  private int distance(BlockPos pos1, BlockPos pos2) {
    return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()) + Math.abs(pos1.getZ() - pos2.getZ());
  }

  private double getPenalty(BlockPos pos) {
    double cost = 0.0D;
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        Block block = getBlockState(pos.add(i, 1, j)).getBlock();
        if (block != Blocks.air && block != Blocks.snow_layer && block != Blocks.snow) {
          cost += 2.0D;
        }
      }
    }
    return cost;
  }


  /**
   * @param pos 要检查的blockPos
   * @return 返回0则代表可以走 返回1则代表因为没有支撑点无法走路 返回2则达标高度不够无法走路
   */
  public int getWalkType(BlockPos pos) {
    Block block = getBlockState(pos.down()).getBlock(); // 找下面的那个方块
    if (block.getMaterial().isLiquid() || (!block.getMaterial().isSolid() && Block.getIdFromBlock(block) != 78)) {
      Block blockFoot = getBlockState(pos).getBlock();
      Block blockHead = getBlockState(pos.add(0, 1, 0)).getBlock();
      if (blockFoot.getMaterial().isSolid() || blockHead.getMaterial().isSolid()) {
        return 2;
      } else {
        return 1;
      }
    }

    double height = 0.0D;
    Block blockFoot = getBlockState(pos).getBlock();
    Block blockHead = getBlockState(pos.add(0, 1, 0)).getBlock();
    Block blockTop = getBlockState(pos.add(0, 2, 0)).getBlock();
    if (blockFoot != Blocks.air) {
      height += blockFoot.getBlockBoundsMaxY();
    }
    if (blockHead != Blocks.air) {
      height += blockHead.getBlockBoundsMaxY();
    }
    if (blockTop != Blocks.air) {
      height += blockTop.getBlockBoundsMaxY();
    }
    return height < 0.6D ? 0 : 2;
  }

  public boolean isBreakable(BlockPos pos) {
    if (!canMineBlocks) {// 如果不能挖掘方块
      return false;
    }
    Block footBlock = getBlockState(pos).getBlock();
    Block headBlock = getBlockState(pos).getBlock();

    List<Block> unbreakableBlocks = Arrays.asList(
        Blocks.wool, Blocks.sand, Blocks.gravel, Blocks.rail,
        Blocks.bedrock
    );

    boolean footBlockBreakable = true;
    boolean headBlockBreakable = true;

    if (unbreakableBlocks.contains(footBlock)) {
      footBlockBreakable = false;
    } else if (specialUnbreakableBlocks.contains(pos)) {
      footBlockBreakable = false;
    }

    if (unbreakableBlocks.contains(headBlock)) {
      headBlockBreakable = false;
    } else if (specialUnbreakableBlocks.contains(pos)) {
      headBlockBreakable = false;
    }

    return footBlockBreakable && headBlockBreakable;
  }

  private IBlockState getBlockState(BlockPos pos) {
    IBlockState state = blockStateMap.get(pos);
    if (state == null) {
      state = mc.theWorld.getBlockState(pos);
      blockStateMap.put(pos, state);
    }

    return state;
  }
}
