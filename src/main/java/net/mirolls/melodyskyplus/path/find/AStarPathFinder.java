package net.mirolls.melodyskyplus.path.find;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Utils.Vec3d;

import java.util.*;

public class AStarPathFinder {
  private final BlockPos[] BASIC_OFFSETS = {
      new BlockPos(1, 0, 0),  // 右
      new BlockPos(-1, 0, 0), // 左
      new BlockPos(0, 0, 1),  // 前
      new BlockPos(0, 0, -1),  // 后
      new BlockPos(0, -1, 0)
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


  public AStarPathFinder(boolean canMineBlocks, boolean jumpBoost, Set<BlockPos> specialUnbreakableBlocks) {
    mc = Minecraft.getMinecraft();
    this.canMineBlocks = canMineBlocks;
    this.specialUnbreakableBlocks = specialUnbreakableBlocks;
    this.jumpBoost = jumpBoost;
  }

  public AStarPathFinder(boolean canMineBlocks, boolean jumpBoost) {
    this(canMineBlocks, jumpBoost, new HashSet<>());
  }

  public AStarPathFinder() {
    this(true, true);
  }

  public static boolean rayTrace(BlockPos from, BlockPos to) {
    Minecraft mc = Minecraft.getMinecraft();

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

  public List<PathPos> findPath(BlockPos start, BlockPos target) {
    
    PathNode root = new PathNode(0, distance(start, target), null, start, PathPos.PathNodeType.WALK);

    if (start.equals(target)) {
      return new ArrayList<>();
      // 如果开局即巅峰 则直接返回
    }
    openedBlocks.add(root);
    visitedPositions.add(root.pos);

    PathNode targetPathNode;

    MelodySkyPlus.LOGGER.info("Start to find path. player pos:" + start + " targetPos:" + target);
    do {
      PathNode nodeToClose = openedBlocks.poll();

      if (nodeToClose == null) {
        return null; // 找不到路径
      }

      PathNode block = closeBlock(nodeToClose, target);

      if (block != null) {
        targetPathNode = block;
        break;
      }
    } while (true);


    return buildPath(targetPathNode); // 关于节点的优化 这里省这部分逻辑 移动到 PathTransferor 和 PathExec 间接运行
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

    PathPos abilityStartPos = null;
    for (PathNode node : paths) {
      if (node.type == PathPos.PathNodeType.ABILITY) {
        // 如果该点的类型是技能
        if (abilityStartPos == null) {
          // 这是第一个技能点
          abilityStartPos = returnPaths.get(returnPaths.size() - 1); // 要在这个点释放技能 记录这个点
        }
      } else {
        if (abilityStartPos == null) {
          // 非技能情况
          if (node.type == PathPos.PathNodeType.JUMP_END && node.pos.getY() <= node.posParent.getY() + 1) {
            // 台阶支持
            IBlockState blockState = getBlockState(node.pos.down());
            if (blockState.getBlock().getRegistryName().contains("slab")) {
              if (blockState.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM) {
                node.type = PathPos.PathNodeType.WALK;
              }
            }
          }
          returnPaths.add(new PathPos(node.type, node.pos));
        } else {
          // 找到终点了
          returnPaths.add(abilityStartPos);
          returnPaths.add(new PathPos(PathPos.PathNodeType.ABILITY_END, node.pos));
          abilityStartPos = null;
        }
      }
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

      if (parent.type == PathPos.PathNodeType.WALK || parent.type == PathPos.PathNodeType.JUMP_END) {
        if (jumpBoost) {
          for (int i = 0; i < 6; i++) {
            BlockPos posFoot = mc.thePlayer.getPosition().add(0, i, 0);
            BlockPos posHead = mc.thePlayer.getPosition().add(0, i + 1, 0);

            if (getBlockState(posFoot).getBlock() != Blocks.air || getBlockState(posHead).getBlock() != Blocks.air) {
              break;
            }

            for (BlockPos offset : getJumpOffsets(i + 1)) {
              PathNode node = openBlock(parent, target, offset, true, true, true);
              if (node != null) return node;
            }
          }
        } else {
          for (BlockPos offset : getJumpOffsets(1)) {
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

  private BlockPos[] getJumpOffsets(int layer) {
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
        PathNode node = new PathNode(parent.gCost + 1 + getPenalty(pos), distance, parent, pos, jumpEnd ? PathPos.PathNodeType.JUMP_END : PathPos.PathNodeType.WALK);
        openedBlocks.add(node);
        visitedPositions.add(node.pos);
        if (distance == 0) {
          return node;
        }
      } else if (walkType == 1 && !disableAbility) {
        int distance = distance(pos, target);
        PathNode node = new PathNode(parent.gCost + 1 + 2, distance, parent, pos, PathPos.PathNodeType.ABILITY);
        openedBlocks.add(node);
        visitedPositions.add(node.pos);
        if (distance == 0) {
          return node;
        }
      } else if (walkType == 2 && !disableMining) {
        int breakable = getBreakable(pos);
        if (breakable != -1 && parent.type != PathPos.PathNodeType.ABILITY) {
          int distance = distance(pos, target);
          PathNode node = new PathNode(parent.gCost + 1 + breakable, distance, parent, pos, PathPos.PathNodeType.MINE);
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

  public int getBreakable(BlockPos pos) {
    if (!canMineBlocks) {// 如果不能挖掘方块
      return -1;
    }
    Block footBlock = getBlockState(pos).getBlock();
    Block headBlock = getBlockState(pos).getBlock();

    List<Block> unbreakableBlocks = Arrays.asList(
        Blocks.wool, Blocks.sand, Blocks.gravel, Blocks.rail,
        Blocks.bedrock
    );

    int returnValue = 0;

    if (unbreakableBlocks.contains(footBlock)) {
      return -1;
    } else if (specialUnbreakableBlocks.contains(pos)) {
      return -1;
    }

    if (unbreakableBlocks.contains(headBlock)) {
      return -1;
    } else if (specialUnbreakableBlocks.contains(pos)) {
      return -1;
    }

    if (footBlock == Blocks.stone) {
      returnValue += 1;
    } else if (footBlock == Blocks.dirt || footBlock == Blocks.grass) {
      returnValue += 6;
    } else {
      returnValue += 20;
    }

    if (headBlock == Blocks.stone) {
      returnValue += 1;
    } else if (footBlock == Blocks.dirt || footBlock == Blocks.grass) {
      returnValue += 6;
    } else {
      returnValue += 20;
    }
    return returnValue;
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
