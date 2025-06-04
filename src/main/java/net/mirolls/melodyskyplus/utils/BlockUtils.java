package net.mirolls.melodyskyplus.utils;

import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.Vec3d;

import java.util.*;

public class BlockUtils {
  private static final Map<StartEndInfo, List<BlockPos>> doubleHeightStoreMap = new HashMap<>();
  private static final Map<StartEndInfo, List<BlockPos>> normalStoreMap = new HashMap<>();

  public static List<BlockPos> getDoubleHeightBlocksBetween(Vec3d startVec, Vec3d endVec) {
    StartEndInfo info = new StartEndInfo(startVec, endVec);
    List<BlockPos> storeValue = doubleHeightStoreMap.get(info);

    if (storeValue != null) return storeValue;

    List<BlockPos> betweens = getBlocksBetween(startVec, endVec);
    Set<BlockPos> resultSet = new HashSet<>(betweens);

    for (BlockPos pos : betweens) {
      if (resultSet.contains(pos.down()) || resultSet.contains(pos.up())) {
        continue;
      }

      resultSet.add(pos.down());
    }

    List<BlockPos> sorted = new ArrayList<>(resultSet);
    sorted.sort(Comparator.comparingDouble(e -> e.distanceSq(startVec.x, startVec.y, startVec.z)));

    doubleHeightStoreMap.put(info, sorted);
    return sorted;
  }

  public static List<BlockPos> getBlocksBetween(Vec3d startVec, Vec3d endVec) {
    StartEndInfo info = new StartEndInfo(startVec, endVec);
    List<BlockPos> storeValue = normalStoreMap.get(info);

    if (storeValue != null) return storeValue;

    List<BlockPos> blocks = new ArrayList<>();

    double startX = startVec.x;
    double startY = startVec.y;
    double startZ = startVec.z;

    double endX = endVec.x;
    double endY = endVec.y;
    double endZ = endVec.z;

    int currentX = (int) Math.floor(startX);
    int currentY = (int) Math.floor(startY);
    int currentZ = (int) Math.floor(startZ);

    int endBlockX = (int) Math.floor(endX);
    int endBlockY = (int) Math.floor(endY);
    int endBlockZ = (int) Math.floor(endZ);

    BlockPos startBlock = new BlockPos(currentX, currentY, currentZ);
    BlockPos endBlock = new BlockPos(endBlockX, endBlockY, endBlockZ);

    blocks.add(startBlock);

    if (startBlock.equals(endBlock)) {
      return blocks;
    }

    double dx = endX - startX;
    double dy = endY - startY;
    double dz = endZ - startZ;

    int stepX = dx == 0 ? 0 : (dx > 0 ? 1 : -1);
    int stepY = dy == 0 ? 0 : (dy > 0 ? 1 : -1);
    int stepZ = dz == 0 ? 0 : (dz > 0 ? 1 : -1);

    double xNext = stepX != 0 ? (currentX + (stepX > 0 ? 1 : 0)) : startX;
    double yNext = stepY != 0 ? (currentY + (stepY > 0 ? 1 : 0)) : startY;
    double zNext = stepZ != 0 ? (currentZ + (stepZ > 0 ? 1 : 0)) : startZ;

    double tMaxX = stepX != 0 ? (xNext - startX) / dx : Double.MAX_VALUE;
    double tMaxY = stepY != 0 ? (yNext - startY) / dy : Double.MAX_VALUE;
    double tMaxZ = stepZ != 0 ? (zNext - startZ) / dz : Double.MAX_VALUE;

    double tDeltaX = stepX != 0 ? (1.0 / Math.abs(dx)) : Double.MAX_VALUE;
    double tDeltaY = stepY != 0 ? (1.0 / Math.abs(dy)) : Double.MAX_VALUE;
    double tDeltaZ = stepZ != 0 ? (1.0 / Math.abs(dz)) : Double.MAX_VALUE;

    while (true) {
      double minT = Math.min(Math.min(tMaxX, tMaxY), tMaxZ);
      List<Integer> axes = new ArrayList<>();
      if (tMaxX == minT) axes.add(0);
      if (tMaxY == minT) axes.add(1);
      if (tMaxZ == minT) axes.add(2);

      for (int axis : axes) {
        switch (axis) {
          case 0:
            currentX += stepX;
            tMaxX += tDeltaX;
            break;
          case 1:
            currentY += stepY;
            tMaxY += tDeltaY;
            break;
          case 2:
            currentZ += stepZ;
            tMaxZ += tDeltaZ;
            break;
        }
      }

      BlockPos currentBlock = new BlockPos(currentX, currentY, currentZ);
      blocks.add(currentBlock);

      if (currentBlock.equals(endBlock)) {
        break;
      }
    }

    normalStoreMap.put(info, blocks);
    return blocks;
  }

  public static double calcDistanceSq(Vec3d start, Vec3d end) {
    double xSq = Math.pow(start.getX() - end.getX(), 2);
    double ySq = Math.pow(start.getY() - end.getY(), 2);
    double zSq = Math.pow(start.getZ() - end.getZ(), 2);

    return xSq + ySq + zSq;
  }
}

class StartEndInfo {
  Vec3d start;
  Vec3d end;

  public StartEndInfo(Vec3d start, Vec3d end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof StartEndInfo)) return false;
    StartEndInfo info = (StartEndInfo) o;
    return Objects.equals(start, info.start) && Objects.equals(end, info.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end);
  }
}