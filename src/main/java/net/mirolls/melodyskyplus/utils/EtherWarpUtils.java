package net.mirolls.melodyskyplus.utils;

import net.minecraft.util.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class EtherWarpUtils {

  public static List<BlockPos> findWayToEtherWarp(BlockPos end, int maxLayer, int radius) {
    return findWayToEtherWarp(1, end, maxLayer, radius);
  }

  public static List<BlockPos> findWayToEtherWarp(int layer, BlockPos end, int maxLayer, int radius) {
    List<BlockPos> routes = findRoutesWithLayer(layer, end, radius);
    if (routes.isEmpty() && layer <= maxLayer) {
      return findWayToEtherWarp(layer + 1, end, maxLayer, radius);
    }

    return routes;
  }

  private static List<BlockPos> findRoutesWithLayer(int layer, BlockPos end, int radius) {
    Map<Integer, Set<EtherWarpPos>> layerMap = new HashMap<>();

    // 先加入第0层
    layerMap.put(0, new HashSet<>());
    layerMap.get(0).add(new EtherWarpPos(end, null));

    Set<BlockPos> allInBlock = getBlocksInArea(end, radius);

    for (int i = 1; i < layer; i++) { // 这里特意少做一层 最后直接用PlayerUtils.rayTrace获取
      // 每一层单独处理
      Set<EtherWarpPos> thisLayer = new HashSet<>();

      for (EtherWarpPos lastPos : layerMap.get(i - 1)) {
        // 面对上一层的所有点展开搜查
        for (BlockPos pos : allInBlock) {
          if (PlayerUtils.rayTrace(pos, lastPos.pos)) {
            // 存入当前层
            thisLayer.add(new EtherWarpPos(pos, lastPos));
          }
        }
      }

      layerMap.put(i, thisLayer);
    }

    // 获取最后一层
    for (EtherWarpPos etherWarpPos : layerMap.get(layer - 1)) {
      if (PlayerUtils.rayTrace(etherWarpPos.pos)) {
        // 找到path了 进行构建
        List<EtherWarpPos> path = new ArrayList<>();
        path.add(etherWarpPos);

        List<EtherWarpPos> builtPath = buildPath(path);

        return builtPath.stream().map((e) -> e.pos).collect(Collectors.toList());
      }
    }

    return new ArrayList<>();
  }

  private static List<EtherWarpPos> buildPath(List<EtherWarpPos> currentPath) {
    if (currentPath.get(currentPath.size() - 1).parent != null) {
      currentPath.add(currentPath.get(currentPath.size() - 1).parent);
      return buildPath(currentPath);
    } else {
      return currentPath;
    }
  }

  private static HashSet<BlockPos> getBlocksInArea(BlockPos target, int radius) {
    BlockPos player = PlayerUtils.getPlayerLocation();

    int x = target.getX() - player.getX() / Math.abs(target.getX() - player.getX());
    int y = target.getY() - player.getY() / Math.abs(target.getY() - player.getY());
    int z = target.getZ() - player.getZ() / Math.abs(target.getZ() - player.getZ());

    Iterable<BlockPos> iterable = BlockPos.getAllInBox(target.add(x * radius, y * radius, z * radius), player.add((-x) * radius, (-y) * radius, (-z) * radius));
    HashSet<BlockPos> set = new HashSet<>();
    for (BlockPos pos : iterable) {
      set.add(pos);
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
