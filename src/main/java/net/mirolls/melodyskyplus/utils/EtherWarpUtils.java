package net.mirolls.melodyskyplus.utils;

import java.util.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;

public class EtherWarpUtils {
  public static List<BlockPos> findWayToEtherWarp(
      BlockPos end, int maxLayer, int radius, int maxRetryChance) {
    return findWayToEtherWarp(end, maxLayer, radius, new BlockStateStoreUtils(), maxRetryChance);
  }

  public static List<BlockPos> findWayToEtherWarp(
      BlockPos end,
      int maxLayer,
      int radius,
      BlockStateStoreUtils blockStateStoreUtils,
      int maxRetryChance) {
    MelodySkyPlus.LOGGER.info("getBlocksInArea has been called");
    long startTime = System.currentTimeMillis();
    Set<BlockPos> blockInArea = getBlocksInArea(end, radius, blockStateStoreUtils);
    MelodySkyPlus.LOGGER.info(
        "getBlocksInArea's return value size: {} in {}ms",
        blockInArea.size(),
        System.currentTimeMillis() - startTime);

    EtherWarpPos lastPos = getLastEtherWarpPos(null, end, blockInArea, 0, maxLayer, maxRetryChance);

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

  private static EtherWarpPos getLastEtherWarpPos(
      EtherWarpPos lastPos,
      BlockPos end,
      Set<BlockPos> blockInArea,
      int layer,
      int maxLayer,
      int maxRetryChance) {
    if (layer > maxLayer) return null;

    if (lastPos == null ? PlayerUtils.rayTrace(end) : PlayerUtils.rayTrace(lastPos.pos, end)) {
      // 如果该点可以直接到达终点 则立即返回
      return new EtherWarpPos(end, lastPos);
    } else {
      // 如果不可以 开始寻找中间点
      int retryTime = 0;
      for (BlockPos pos : blockInArea) {
        if (retryTime > maxRetryChance) {
          if (lastPos == null
              ? PlayerUtils.rayTrace(pos)
              : PlayerUtils.rayTrace(lastPos.pos, pos)) {
            // 如果可以etherWarp到
            EtherWarpPos loopResult =
                getLastEtherWarpPos(
                    new EtherWarpPos(pos, lastPos),
                    end,
                    blockInArea,
                    layer + 1,
                    maxLayer,
                    maxRetryChance);
            if (loopResult != null) {
              return loopResult;
            } else {
              retryTime++;
            }
          }
        }
      }
      // 如果完毕之后还没有返回 则无法找到路径 返回null
      return null;
    }
  }

  private static Set<BlockPos> getBlocksInArea(
      BlockPos target, int radius, BlockStateStoreUtils store) {
    BlockPos player = PlayerUtils.getPlayerLocation();

    if (target.equals(player)) {
      return new HashSet<>();
    }

    int dx = Integer.compare(target.getX(), player.getX());
    int dy = Integer.compare(target.getY(), player.getY());
    int dz = Integer.compare(target.getZ(), player.getZ());

    BlockPos start = target.add(dx * radius, dy * radius, dz * radius);
    BlockPos end = player.add(-dx * radius, -dy * radius, -dz * radius);
    Iterable<BlockPos> iterable = BlockPos.getAllInBox(start, end);
    HashSet<BlockPos> set = new HashSet<>();

    int loop = 0;
    for (BlockPos pos : iterable) {
      loop++;
      if (loop > 200000) {
        MelodySkyPlus.LOGGER.info("Loop time is bigger than 200000, maybe meet infinite loop.");
        MelodySkyPlus.LOGGER.info("========= Debug Information =========");
        MelodySkyPlus.LOGGER.info("Two point: {}, {}", start, end);
        MelodySkyPlus.LOGGER.info("Radius: {}", radius);
        MelodySkyPlus.LOGGER.info("PlayerLocation: {}", player);
        MelodySkyPlus.LOGGER.info("Target: {}", target);
        MelodySkyPlus.LOGGER.info("=====================================");
        break;
      }
      // 只有可到达的ether warp点
      if (store.getBlockState(pos).getBlock().getMaterial().isSolid()) {
        if (store.getBlockState(pos.up()).getBlock() == Blocks.air
            && store.getBlockState(pos.up().up()).getBlock() == Blocks.air) {
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
