package net.mirolls.melodyskyplus.path.optimization;

import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.path.find.PathPos;
import net.mirolls.melodyskyplus.path.type.Node;
import xyz.Melody.Utils.Vec3d;

public class PathOptimizer {
  private final Map<BlockPos, IBlockState> blockStateMap = new HashMap<>();
  public ArrayList<Vec3d> routeVec = new ArrayList<>();

  private IBlockState getBlockState(BlockPos pos) {
    Minecraft mc = Minecraft.getMinecraft();
    IBlockState state = blockStateMap.get(pos);
    if (state == null) {
      state = mc.theWorld.getBlockState(pos);
      blockStateMap.put(pos, state);
    }

    return state;
  }

  public List<Node> optimize(List<PathPos> path) {
    List<PathPos> nodes = new ArrayList<>();

    if (path.size() < 2) return Node.fromPathPosList(path);

    PathPos lastNode = null;

    for (int i = 0; i < path.size(); i++) {
      PathPos pos = path.get(i);
      if (i == 0) {
        // 如果是第一个节点
        nodes.add(pos);
      } else {
        // 遍历每一个PathPos 开始研究能不能走到这个PathPos
        if (pos.getType() == PathPos.PathNodeType.WALK) {
          PathPos prevPos = path.get(i - 1);
          if (canGo(nodes.get(nodes.size() - 1).getPos(), prevPos.getPos(), pos.getPos())) {
            lastNode = pos;
          } else {
            // 如果出现了无法走到该pos的情况
            // 则需要添加该pos的前一个可以走的pos
            if (lastNode != null) {
              nodes.add(lastNode);
              lastNode = null;

              // i不加1 重新从这个点开始算
              i--;
            }
          }
        } else {
          // 如果该节点的类型不是走路 则需要添加(并且一并添加好走路前的节点)
          if (lastNode != null) {
            // 添加好特殊节点前的一个节点(理论上来说是走路节点)
            nodes.add(lastNode);
            lastNode = null;
          }
          nodes.add(pos);
        }
      }
    }

    if (!nodes.contains(lastNode) && lastNode != null) nodes.add(lastNode);

    nodes.removeIf(Objects::isNull); // 移除空的

    return Node.fromPathPosList(nodes);
  }

  public boolean canGo(BlockPos startPos, BlockPos prevNode, BlockPos target) {
    if (prevNode.equals(startPos)) {
      return true;
    }
    if (startPos.getY() == target.getY()) {
      if (startPos.getX() == target.getX()) {
        // X 或者 Z 中需要至少有一个相同的 不相同的那个应当差1
        if (Math.abs(startPos.getZ() - target.getZ()) == 1) {
          return true;
        }
      } else if (startPos.getZ() == target.getZ()) {
        // X 或者 Z 中需要至少有一个相同的 不相同的那个应当差1
        if (Math.abs(startPos.getX() - target.getX()) == 1) {
          return true;
        }
      }
    }

    int yPos = startPos.getY();
    List<BlockPos> posList = calculate(startPos, target);
    posList.sort(
        Comparator.comparing(
            blockPos ->
                Math.hypot(blockPos.getX() - startPos.getX(), blockPos.getZ() - startPos.getZ())));

    // 记录所有的半砖位置
    Set<BlockPos> bottomHalfSlabs = new HashSet<>();

    for (BlockPos pos : posList) {
      BlockPos bp = new BlockPos(pos.getX(), yPos, pos.getZ());
      IBlockState blockStateGround = getBlockState(bp.down());
      IBlockState blockStateFoot = getBlockState(bp);
      IBlockState blockStateHead = getBlockState(bp.up());

      // 头上方块被挡住 则毫无办法 直接处理
      if (blockStateHead.getBlock() != Blocks.air) {
        return false;
      }

      // 是否有路径阻拦
      if (blockStateFoot.getBlock() != Blocks.air && blockStateFoot.getBlock() != Blocks.carpet) {
        if (blockStateFoot.getBlock().getRegistryName().contains("slab")
            && !blockStateFoot.getBlock().getRegistryName().contains("double")
            && BlockSlab.EnumBlockHalf.BOTTOM == blockStateFoot.getValue(BlockSlab.HALF)) {
          // 如果是下半砖的 检查一下更高头上的能不能跑
          if (getBlockState(bp.add(0, 2, 0)).getBlock() == Blocks.air) {
            // 额外检查一下距离
            if (checkDistance(Vec3d.ofCenter(bp), 0.8)) {
              bottomHalfSlabs.add(bp);
            }
          } else {
            // 要求头上至少有2格子宽
            return false;
          }
        } else {
          // 研究 该方块是不是贴着半砖的
          for (BlockPos slab : bottomHalfSlabs) {
            if (slab.getY() == bp.getY()) {
              if (Math.abs(slab.getX() - bp.getX()) <= 1
                  && Math.abs(slab.getX() - bp.getX()) <= 1) {
                // 如果是贴着半砖的 则对yPos进行+1处理 并且重新构建
                yPos += 1;
                bp = new BlockPos(pos.getX(), yPos, pos.getZ());
                blockStateGround = getBlockState(bp.down());
                blockStateHead = getBlockState(bp.up());
                // Foot不用检查 因为就是上一部的head

                if (blockStateHead.getBlock() != Blocks.air) {
                  // 依然需要检查 避免升高一格后出现意外情况
                  return false;
                }
              }
            }
          }
        }
      }

      // 检测脚底下能不能走的
      if (!blockStateGround.getBlock().getMaterial().isSolid()) {
        // 这种情况自行处理
        return false;
      }
    }
    return true;
  }

  private ArrayList<BlockPos> calculate(BlockPos start, BlockPos end) {
    routeVec = updateVec(start, end);
    if (!routeVec.isEmpty()) {

      int minX = Math.min(start.getX(), end.getX());
      int maxX = Math.max(start.getX(), end.getX());
      int minZ = Math.min(start.getZ(), end.getZ());
      int maxZ = Math.max(start.getZ(), end.getZ());
      ArrayList<Vec3d> blocks = new ArrayList<>();

      for (int x = minX; x <= maxX; ++x) {
        for (int z = minZ; z <= maxZ; ++z) {
          BlockPos blockPos = new BlockPos(x, start.getY(), z);
          blocks.add(Vec3d.ofCenter(blockPos));
        }
      }

      blocks.removeIf((vec3d) -> checkDistance(vec3d, 2.0D));

      ArrayList<BlockPos> poses = new ArrayList<>();

      for (Vec3d v3d : blocks) {
        poses.add(new BlockPos(v3d.getX(), v3d.getY(), v3d.getZ()));
      }

      return poses.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    return new ArrayList<>();
  }

  private ArrayList<Vec3d> updateVec(BlockPos start, BlockPos end) {
    ArrayList<Vec3d> vecPoses = new ArrayList<>();

    double deltaX = end.getX() - start.getX();
    // double deltaY = end.getY() - start.getY();
    double deltaZ = end.getZ() - start.getZ();
    double maxDist = Math.max(Math.abs(deltaX), /*Math.abs(deltaY),*/ Math.abs(deltaZ)) * 25.0;
    double incX = deltaX / maxDist;
    // double incY = deltaY / maxDist;
    double incZ = deltaZ / maxDist;

    Vec3d startVec = Vec3d.ofCenter(start);
    double xPos = startVec.getX();
    // double yPos = startVec.getY();
    double zPos = startVec.getZ();

    for (int huh = 1; (double) huh <= maxDist; ++huh) {
      xPos += incX;
      // yPos += incY;
      zPos += incZ;
      vecPoses.add(new Vec3d(xPos, /* yPos */ start.getY(), zPos));
    }

    return vecPoses;
  }

  private boolean checkDistance(Vec3d vec, double value) {
    Iterator<Vec3d> var2 = routeVec.iterator();

    boolean x;
    // boolean y;
    boolean z;
    do {
      if (!var2.hasNext()) {
        return true;
      }

      Vec3d v = var2.next();
      x = Math.abs(v.getX() - vec.getX()) <= value;
      // y = Math.abs(v.getY() - vec.getY()) <= 1.0;
      z = Math.abs(v.getZ() - vec.getZ()) <= value;
    } while (!x || /*!y ||*/ !z);

    return false;
  }
}
