package net.mirolls.melodyskyplus.utils;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.Vec3d;

public class BlockUtils {
  // 使用LRU缓存限制内存增长 (最大512条路径)
  private static final int MAX_CACHE_SIZE = 512;
  private static final Map<StartEndInfo, List<BlockPos>> doubleHeightStoreMap =
      Collections.synchronizedMap(new LruCache<>(MAX_CACHE_SIZE));
  private static final Map<StartEndInfo, List<BlockPos>> normalStoreMap =
      Collections.synchronizedMap(new LruCache<>(MAX_CACHE_SIZE));

  // 线程安全优化
  private static final ReadWriteLock lock = new ReentrantReadWriteLock();

  // 手动清空缓存的API
  public static void clearCaches() {
    doubleHeightStoreMap.clear();
    normalStoreMap.clear();
  }

  public static List<BlockPos> getDoubleHeightBlocksBetween(Vec3d startVec, Vec3d endVec) {
    // 超长路径直接计算不缓存 (距离平方>1,000,000)
    if (calcDistanceSq(startVec, endVec) > 1_000_000) {
      return calculateDoubleHeightWithoutCache(startVec, endVec);
    }

    StartEndInfo info = new StartEndInfo(startVec, endVec);

    // 读缓存
    lock.readLock().lock();
    try {
      List<BlockPos> cached = doubleHeightStoreMap.get(info);
      if (cached != null) return new ArrayList<>(cached); // 返回副本防止外部修改
    } finally {
      lock.readLock().unlock();
    }

    // 缓存未命中时计算
    List<BlockPos> result = calculateDoubleHeightWithoutCache(startVec, endVec);

    // 写缓存
    lock.writeLock().lock();
    try {
      doubleHeightStoreMap.put(info, new ArrayList<>(result)); // 存储副本
      return result;
    } finally {
      lock.writeLock().unlock();
    }
  }

  private static List<BlockPos> calculateDoubleHeightWithoutCache(Vec3d startVec, Vec3d endVec) {
    List<BlockPos> betweens = getBlocksBetween(startVec, endVec);
    Set<BlockPos> resultSet = new HashSet<>(betweens.size() * 2);

    // 添加原始路径块
    resultSet.addAll(betweens);

    // 添加下层块（如果相邻块不存在）
    for (BlockPos pos : betweens) {
      BlockPos downPos = pos.down();
      if (!resultSet.contains(downPos)
          && !resultSet.contains(downPos.down())
          && !resultSet.contains(downPos.up())) {
        resultSet.add(downPos);
      }
    }

    // 按距离排序
    List<BlockPos> sorted = new ArrayList<>(resultSet);
    final Vec3d finalStart = startVec;
    sorted.sort(
        Comparator.comparingDouble(
            pos -> pos.distanceSq(finalStart.x, finalStart.y, finalStart.z)));

    return sorted;
  }

  public static List<BlockPos> getBlocksBetween(Vec3d startVec, Vec3d endVec) {
    // 超长路径直接计算不缓存
    if (calcDistanceSq(startVec, endVec) > 1_000_000) {
      return calculateBlocksWithoutCache(startVec, endVec);
    }

    StartEndInfo info = new StartEndInfo(startVec, endVec);

    // 读缓存
    lock.readLock().lock();
    try {
      List<BlockPos> cached = normalStoreMap.get(info);
      if (cached != null) return new ArrayList<>(cached);
    } finally {
      lock.readLock().unlock();
    }

    // 缓存未命中时计算
    List<BlockPos> result = calculateBlocksWithoutCache(startVec, endVec);

    // 写缓存
    lock.writeLock().lock();
    try {
      normalStoreMap.put(info, new ArrayList<>(result));
      return result;
    } finally {
      lock.writeLock().unlock();
    }
  }

  private static List<BlockPos> calculateBlocksWithoutCache(Vec3d startVec, Vec3d endVec) {
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
      List<Integer> axes = new ArrayList<>(3);
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

    return blocks;
  }

  public static double calcDistanceSq(Vec3d start, Vec3d end) {
    double dx = start.x - end.x;
    double dy = start.y - end.y;
    double dz = start.z - end.z;
    return dx * dx + dy * dy + dz * dz;
  }
}

// LRU缓存实现 (最近最少使用淘汰)
class LruCache<K, V> extends LinkedHashMap<K, V> {
  private final int maxSize;

  public LruCache(int maxSize) {
    super(16, 0.75f, true);
    this.maxSize = maxSize;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > maxSize;
  }
}

// 路径信息键
class StartEndInfo {
  final Vec3d start;
  final Vec3d end;

  public StartEndInfo(Vec3d start, Vec3d end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StartEndInfo)) return false;
    StartEndInfo that = (StartEndInfo) o;
    return Objects.equals(start, that.start) && Objects.equals(end, that.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end);
  }
}
