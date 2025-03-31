package net.mirolls.melodyskyplus.path;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.Vec3d;

import java.util.*;
import java.util.stream.Collectors;

public class PathOptimizer {
  public final List<PathPos> importantNodes = new ArrayList<>();
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

  public void optimize(List<PathPos> path) {
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

    if (lastPathPos != null) {
      importantNodes.add(lastPathPos);
    }
  }

  public boolean canGo(BlockPos startPos, BlockPos target) {
    int yPos = startPos.getY();
    for (BlockPos pos : calculate(startPos, target)) {
      BlockPos bp = new BlockPos(pos.getX(), yPos, pos.getZ());
      IBlockState blockState = getBlockState(bp);
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

      if (getBlockState(bp).getBlock() != Blocks.air || getBlockState(bp.up()).getBlock() != Blocks.air) {
        return false;
      }

      // 检测脚底下能不能走的
      if (!getBlockState(bp.down()).getBlock().getMaterial().isSolid()) {
        return false;
      }
    }
    return true;
  }

  private ArrayList<BlockPos> calculate(BlockPos start, BlockPos end) {
    routeVec = updateVec(start, end);
    if (!routeVec.isEmpty()) {

      int minX = Math.min(start.getX(), end.getX()) - 5;
      int maxX = Math.max(start.getX(), end.getX()) + 5;
      int minZ = Math.min(start.getZ(), end.getZ()) - 5;
      int maxZ = Math.max(start.getZ(), end.getZ()) + 5;
      ArrayList<Vec3d> blocks = new ArrayList<>();

      for (int x = minX; x <= maxX; ++x) {
        for (int z = minZ; z <= maxZ; ++z) {
          BlockPos blockPos = new BlockPos(x, start.getY(), z);
          blocks.add(Vec3d.ofCenter(blockPos));
        }
      }

      blocks.removeIf(this::checkDistance);

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
    double xPos = start.getX();
    // double yPos = start.getY();
    double zPos = start.getZ();

    for (int huh = 1; (double) huh <= maxDist; ++huh) {
      xPos += incX;
      // yPos += incY;
      zPos += incZ;
      vecPoses.add(new Vec3d(xPos, /* yPos */ start.getY(), zPos));
    }

    return vecPoses;
  }

  private boolean checkDistance(Vec3d vec) {
    Iterator<Vec3d> var2 = routeVec.iterator();

    boolean x;
    // boolean y;
    boolean z;
    do {
      if (!var2.hasNext()) {
        return true;
      }

      Vec3d v = var2.next();
      x = Math.abs(v.getX() - vec.getX()) <= 1.6;
      // y = Math.abs(v.getY() - vec.getY()) <= 1.0;
      z = Math.abs(v.getZ() - vec.getZ()) <= 1.6;
    } while (!x || /*!y ||*/ !z);

    return false;
  }
}
