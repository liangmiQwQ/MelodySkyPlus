package net.mirolls.melodyskyplus.utils;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class EtherWarpUtils {
  public static List<BlockPos> findWayToEtherWarp(BlockPos end, int maxLayer, int radius) {
    return findWayToEtherWarp(end, maxLayer, radius, new BlockStateStoreUtils());
  }

  public static List<BlockPos> findWayToEtherWarp(BlockPos end, int maxLayer, int radius, BlockStateStoreUtils blockStateStoreUtils) {
    HashSet<BlockPos> blockInArea = getBlocksInArea(end, radius, blockStateStoreUtils);

    EtherWarpPos lastPos = getLastEtherWarpPos(null, end, blockInArea, 0, maxLayer);

    List<BlockPos> route = new ArrayList<>();

    if (lastPos != null) {
      buildRoute(lastPos, route);
      Collections.reverse(route);
    }

    // 如果找不到路径返回空数组
    return route;
  }

  private static void buildRoute(EtherWarpPos pos, List<BlockPos> route) {
    route.add(pos.pos);

    if (pos.parent != null) {
      buildRoute(pos.parent, route);
    }
  }

  private static EtherWarpPos getLastEtherWarpPos(EtherWarpPos lastPos, BlockPos end, HashSet<BlockPos> blockInArea, int layer, int maxLayer) {
    if (layer > maxLayer) return null;

    if (lastPos == null ? PlayerUtils.rayTrace(end) : PlayerUtils.rayTrace(lastPos.pos, end)) {
      // 如果该点可以直接到达终点 则立即返回
      return new EtherWarpPos(end, lastPos);
    } else {
      // 如果不可以 开始寻找中间点
      for (BlockPos pos : blockInArea) {
        if (lastPos == null ? PlayerUtils.rayTrace(pos) : PlayerUtils.rayTrace(lastPos.pos, pos)) {
          // 如果可以etherWarp到
          EtherWarpPos loopResult = getLastEtherWarpPos(new EtherWarpPos(pos, lastPos), end, blockInArea, layer + 1, maxLayer);
          if (loopResult != null) {
            return loopResult;
          }
        }
      }
      // 如果完毕之后还没有返回 则无法找到路径 返回null
      return null;
    }
  }


  private static HashSet<BlockPos> getBlocksInArea(BlockPos target, int radius, BlockStateStoreUtils store) {
    BlockPos player = PlayerUtils.getPlayerLocation();

    int x = target.getX() - player.getX() == 0 ? 0 : target.getX() - player.getX() / Math.abs(target.getX() - player.getX());
    int y = target.getY() - player.getY() == 0 ? 0 : target.getY() - player.getY() / Math.abs(target.getY() - player.getY());
    int z = target.getZ() - player.getZ() == 0 ? 0 : target.getZ() - player.getZ() / Math.abs(target.getZ() - player.getZ());

    Iterable<BlockPos> iterable = BlockPos.getAllInBox(target.add(x * radius, y * radius, z * radius), player.add((-x) * radius, (-y) * radius, (-z) * radius));
    HashSet<BlockPos> set = new HashSet<>();

    for (BlockPos pos : iterable) {
      // 只有可到达的ether warp点
      if (store.getBlockState(pos).getBlock().getMaterial().isSolid()) {
        if (store.getBlockState(pos.up()).getBlock() == Blocks.air && store.getBlockState(pos.up().up()).getBlock() == Blocks.air) {
          set.add(pos);
        }
      }
    }
    return set;
  }
}

class EtherWarpPos {
  BlockPos pos;
  EtherWarpPos parent;

  public EtherWarpPos(BlockPos pos, EtherWarpPos parent) {
    this.pos = pos;
    this.parent = parent;
  }
}


