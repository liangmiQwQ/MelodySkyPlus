package net.mirolls.melodyskyplus.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.Helper;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class GoldUtils {
  public static BlockPos findGoldToRotate(int findGoldRadius) {
    Minecraft mc = Minecraft.getMinecraft();
    Helper.sendMessage("Finding Gold...");
    final BlockPos playerPos = PlayerUtils.getPlayerLocation();
    final int playerX = playerPos.getX();
    final int playerY = playerPos.getY();
    final int playerZ = playerPos.getZ();

    // 方向偏移数组（东、西、北、南、下）
    final BlockPos[] directionOffsets = {
        new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
        new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
        new BlockPos(0, -1, 0)
    };

    // 使用优先队列按缩放后距离排序（避免平方根计算）
    PriorityQueue<BlockPos> searchQueue = new PriorityQueue<>((a, b) -> {
      double distA = getScaledDistanceSq(a, playerX, playerY, playerZ);
      double distB = getScaledDistanceSq(b, playerX, playerY, playerZ);
      return Double.compare(distA, distB);
    });

    // 生成搜索区域（球形范围）
    generateSphericalSearchArea(playerPos, findGoldRadius, searchQueue);

    BlockPos replaceBlock = null;
    Set<BlockPos> checkedBlocks = new HashSet<>();

    while (!searchQueue.isEmpty()) {
      BlockPos blockPos = searchQueue.poll();
      if (!checkedBlocks.add(blockPos)) continue; // 跳过已检查方块

      // 快速空气检查
      if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.air) continue;

      // 光线追踪检查
      if (!PlayerUtils.rayTrace(blockPos)) continue;

      // 垂直空间检查
      if (!checkVerticalSpace(blockPos)) continue;

      // 金块直接返回
      if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.gold_block) {
        if (checkSurroundingGold(blockPos, directionOffsets)) {
          return blockPos;
        }
      }
      // 记录可替换方块
      else if (replaceBlock == null) {
        if (checkSurroundingGoldCount(blockPos, directionOffsets) >= 3) {
          replaceBlock = blockPos;
        }
      }
    }

    if (replaceBlock != null) {
      Helper.sendMessage("Cannot Find Gold Blocks. Will teleport you to random block near a gold block.");
      return replaceBlock;
    }

    Helper.sendMessage("Cannot Find any blocks near Gold at all. Maybe you're out of Mines of Divan.");
    return null;
  }

  // --- 辅助方法 ---
  private static double getScaledDistanceSq(BlockPos pos, int px, int py, int pz) {
    int dx = pos.getX() - px;
    int dz = pos.getZ() - pz;
    int dy = (pos.getY() - py) * 5; // Y轴缩放因子
    return dx * dx + dz * dz + dy * dy; // 使用平方距离比较
  }

  private static void generateSphericalSearchArea(BlockPos center, int radius, PriorityQueue<BlockPos> queue) {
    // 使用球形区域生成算法（3D中点画圆算法变体）
    for (int x = -radius; x <= radius; x++) {
      for (int z = -radius; z <= radius; z++) {
        for (int y = -radius / 5; y <= radius / 5; y++) {
          BlockPos pos = center.add(x, y, z);
          if (getScaledDistanceSq(pos, center.getX(), center.getY(), center.getZ()) <= radius * radius) {
            queue.add(pos);
          }
        }
      }
    }
  }

  private static boolean checkVerticalSpace(BlockPos pos) {
    Minecraft mc = Minecraft.getMinecraft();

    return mc.theWorld.getBlockState(pos.up()).getBlock() == Blocks.air
        && mc.theWorld.getBlockState(pos.up().up()).getBlock() == Blocks.air;
  }

  private static boolean checkSurroundingGold(BlockPos pos, BlockPos[] directions) {
    Minecraft mc = Minecraft.getMinecraft();

    int goldCount = 0;
    for (BlockPos dir : directions) {
      if (mc.theWorld.getBlockState(pos.add(dir)).getBlock() == Blocks.gold_block) {
        if (++goldCount >= 3) return true;
      }
    }
    return false;
  }

  private static int checkSurroundingGoldCount(BlockPos pos, BlockPos[] directions) {
    Minecraft mc = Minecraft.getMinecraft();

    int count = 0;
    for (BlockPos dir : directions) {
      if (mc.theWorld.getBlockState(pos.add(dir)).getBlock() == Blocks.gold_block) {
        count++;
      }
    }
    return count;
  }
}
