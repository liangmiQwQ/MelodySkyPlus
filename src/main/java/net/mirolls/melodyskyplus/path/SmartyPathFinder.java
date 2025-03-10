package net.mirolls.melodyskyplus.path;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Utils.Vec3d;

import java.util.*;

public class SmartyPathFinder {
  private final BlockPos[] BASIC_OFFSETS = {
      new BlockPos(1, 0, 0),  // 右
      new BlockPos(-1, 0, 0), // 左
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
  private final boolean canMineBlocks;
  private final Minecraft mc;
  private final boolean jumpBoost;


  public SmartyPathFinder(boolean canMineBlocks, boolean jumpBoost, Set<BlockPos> specialUnbreakableBlocks) {
    mc = Minecraft.getMinecraft();
    this.canMineBlocks = canMineBlocks;
    this.specialUnbreakableBlocks = specialUnbreakableBlocks;
    this.jumpBoost = jumpBoost;
  }

  public SmartyPathFinder(boolean canMineBlocks, boolean jumpBoost) {
    this(canMineBlocks, jumpBoost, new HashSet<>());
  }

  public SmartyPathFinder() {
    this(true, true);
  }

  public List<PathPos> findPath(BlockPos target) {
    int x = (int) (mc.thePlayer.posX - mc.thePlayer.posX % 1);
    int y = (int) (mc.thePlayer.posY - mc.thePlayer.posY % 1);
    int z = (int) (mc.thePlayer.posZ - mc.thePlayer.posZ % 1);
    BlockPos posPlayer = new BlockPos(x, y, z); // Minecraft提供的.getPosition不好用 返回的位置经常有较大的误差 这样是最保险的
    PathNode root = new PathNode(0, distance(posPlayer, target), null, posPlayer, PathNodeType.WALK);

    if (posPlayer.equals(target)) {
      return new ArrayList<>();
      // 如果开局即巅峰 则直接返回
    }
    openedBlocks.add(root);
    visitedPositions.add(root.pos);

    PathNode targetPathNode;

    MelodySkyPlus.LOGGER.info("Start to find path. player pos:" + posPlayer + " targetPos:" + target);
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

      if (nodeToClose == null) {
        return null; // 找不到路径
      }

      PathNode block = closeBlock(nodeToClose, target);

      if (block != null) {
        targetPathNode = block;
        break;
      }
    } while (true);


    return buildPath(targetPathNode); // TODO: 增加返回 优化节点 剔除冗余等
  }

  private List<PathPos> buildPath(PathNode endNode) {
    MelodySkyPlus.LOGGER.info("Building Paths");
    LinkedList<PathNode> paths = new LinkedList<>();
    List<PathPos> returnPaths = new ArrayList<>();

    paths.add(endNode);
    if (endNode == null) return null;

    while (true) {
      PathNode node = paths.get(0);
      if (node.nodeParent == null) break;
      paths.addFirst(node.nodeParent);
    }

    PathPos startAbilityNode = null;
    List<PathPos> savedPathPos = new ArrayList<>();
    for (PathNode node : paths) {
      if (node.type == PathNodeType.ABILITY) {
        // 如果需要用技能
        if (startAbilityNode == null) {
          // 并且没有存储过释放技能钱的点
          startAbilityNode = returnPaths.get(returnPaths.size() - 1); // 就存储这个点 在这个点释放技能
        }
        savedPathPos.add(new PathPos(node.type, node.pos));
        // 放入缓存数组
      } else {
        if (startAbilityNode == null) {
          // 正常的跑 就让它跑
          returnPaths.add(new PathPos(node.type, node.pos));
        } else {
          // 找到终点了
          if (rayTrace(startAbilityNode.getPos(), node.pos.down() /*看这个方块下面这个 落脚点*/)
              && getBlockState(node.pos.down()).getBlock().getMaterial().isSolid()) {
            // 如果是可以直接看到的
            startAbilityNode = null;
            savedPathPos = new ArrayList<>();
            returnPaths.add(new PathPos(PathNodeType.ABILITY_ETHER_WARP, node.pos));
          } else {
            // 如果是不能看到的 留给普通的aotv吧
            // TODO: 优化普通aotv节点
            returnPaths.addAll(savedPathPos);
            startAbilityNode = null;
            savedPathPos = new ArrayList<>();
          }
        }
      }
//      returnPaths.add(new PathPos(node.type, node.pos));
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
      openedBlocks.remove(parent);

      for (BlockPos offset : BASIC_OFFSETS) {
        PathNode node = openBlock(parent, target, offset, false, false, false);
        if (node != null) return node;
      }

      // 跳跃部分
      if (distance(parent.pos.up(), target) < distance(parent.pos, target) && parent.type == PathNodeType.WALK || parent.type == PathNodeType.JUMP_END) {
        if (jumpBoost) {
          for (int i = 0; i < 6; i++) {
            BlockPos posFoot = mc.thePlayer.getPosition().add(0, i, 0);
            BlockPos posHead = mc.thePlayer.getPosition().add(0, i + 1, 0);

            if (getBlockState(posFoot).getBlock() != Blocks.air || getBlockState(posHead).getBlock() != Blocks.air) {
              break;
            }

            for (BlockPos offset : getOffsets(i + 1)) {
              PathNode node = openBlock(parent, target, offset, true, true, true);
              if (node != null) return node;
            }
          }
        } else {
          for (BlockPos offset : getOffsets(1)) {
            BlockPos posFoot = mc.thePlayer.getPosition().add(0, 1, 0);
            BlockPos posHead = mc.thePlayer.getPosition().add(0, 2, 0);
            if (getBlockState(posFoot).getBlock() != Blocks.air || getBlockState(posHead).getBlock() != Blocks.air) {
              break;
            }
            PathNode node = openBlock(parent, target, offset, true, false, true);
            if (node != null) return node;
          }
        }
      }
    }
    return null;
  }

  private BlockPos[] getOffsets(int layer) {
    return new BlockPos[]{
        new BlockPos(1, layer, 0),
        new BlockPos(0, layer, 1),
        new BlockPos(-1, layer, 0),
        new BlockPos(0, layer, -1)
    };
  }

  private PathNode openBlock(PathNode parent, BlockPos target, BlockPos offset, boolean jumpEnd, boolean disableMining, boolean disableAbility) {
    BlockPos pos = parent.pos.add(offset);

    boolean posChecked = isPosChecked(pos);

    if (!posChecked) {
      int walkType = getWalkType(pos);
      if (walkType == 0) {
        int distance = distance(pos, target);
        PathNode node = new PathNode(parent.gCost + 1 + getPenalty(pos), distance, parent, pos, jumpEnd ? PathNodeType.JUMP_END : PathNodeType.WALK);
        openedBlocks.add(node);
        visitedPositions.add(node.pos);
        if (distance == 0) {
          return node;
        }
      } else if (walkType == 1 && !disableAbility) {
        int distance = distance(pos, target);
        PathNode node = new PathNode(parent.gCost + 1 + 2, distance, parent, pos, PathNodeType.ABILITY);
        openedBlocks.add(node);
        visitedPositions.add(node.pos);
        if (distance == 0) {
          return node;
        }
      } else if (walkType == 2 && !disableMining) {
        if (isBreakable(pos) && parent.type != PathNodeType.ABILITY) {
          int distance = distance(pos, target);
          PathNode node = new PathNode(parent.gCost + 1 + 2, distance, parent, pos, PathNodeType.MINE);
          openedBlocks.add(node);
          visitedPositions.add(node.pos);
          if (distance == 0) {
            return node;
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
        return 3; // 无敌情况 舍去
      } else {
        return 1;
      }
    }

    double height = 0.0D;
    Block blockFoot = getBlockState(pos).getBlock();
    Block blockHead = getBlockState(pos.add(0, 1, 0)).getBlock();
    if (blockFoot != Blocks.air) {
      height += blockFoot.getBlockBoundsMaxY();
    }
    if (blockHead != Blocks.air) {
      height += blockHead.getBlockBoundsMaxY();
    }
    return height < 0.2D ? 0 : 2;
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

  private boolean rayTrace(BlockPos from, BlockPos to) {
    Vec3d target = null;
    Vec3d[] var2 = Vec3d.points(to);
    int var3 = var2.length;
    int var4 = 0;

    while (var4 < var3) {
      Vec3d vec = var2[var4];
      Vec3 playerVec = new Vec3(from.getX(), from.getY() + 0.6, from.getZ());
      MovingObjectPosition trajectory = mc.theWorld.rayTraceBlocks(playerVec, vec.toVec3(), false, true, true);
      if (trajectory == null) {
        target = vec;
        break;
      }

      label69:
      {
        if (trajectory.entityHit == null || trajectory.entityHit == mc.thePlayer) {
          if (trajectory.getBlockPos() == null) {
            break label69;
          }

          boolean sameX = trajectory.getBlockPos().getX() == to.getX();
          boolean sameY = trajectory.getBlockPos().getY() == to.getY();
          boolean sameZ = trajectory.getBlockPos().getZ() == to.getZ();
          if (sameX && sameY && sameZ) {
            break label69;
          }
        }

        ++var4;
        continue;
      }

      target = vec;
      break;
    }

    return target != null;
  }
}
