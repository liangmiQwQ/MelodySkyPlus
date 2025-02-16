package net.mirolls.melodyskyplus.libs;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class TPCheckDetector {
  public static double lastMotionZ;
  public static double lastMotionX;
  private static BlockPos[] environment;

  private static BlockPos[] createEnvironmentSnapshot() {
    Minecraft mc = Minecraft.getMinecraft();
    BlockPos center = mc.thePlayer.getPosition();
    int radius = 8;

    List<BlockPos> blocks = new ArrayList<>();
    for (int y = -radius; y <= radius; y++) {
      for (int x = -radius; x <= radius; x++) {
        for (int z = -radius; z <= radius; z++) {
          blocks.add(center.add(x, y, z));
        }
      }
    }

    return blocks.toArray(new BlockPos[0]);
  }

  public static void saveEnvironment() {
    environment = createEnvironmentSnapshot();
  }

  private static double calculateEnvironmentDiff(BlockPos[] oldEnv, BlockPos[] newEnv) {
    Map<String, Integer> oldEnvBlocksTypeMap = new HashMap<>();
    List<String> oldEnvBlocksType = getEnvBlocksType(oldEnv);
    Map<String, Integer> newEnvBlocksTypeMap = new HashMap<>();
    List<String> newEnvBlocksType = getEnvBlocksType(newEnv);

    for (String oldEnvBlockType : oldEnvBlocksType) {
      oldEnvBlocksTypeMap.put(oldEnvBlockType, oldEnvBlocksTypeMap.getOrDefault(oldEnvBlockType, 0) + 1);
    }
    for (String newEnvBlockType : newEnvBlocksType) {
      newEnvBlocksTypeMap.put(newEnvBlockType, newEnvBlocksTypeMap.getOrDefault(newEnvBlockType, 0) + 1);
    }

    double diff = 0;
    for (String blockType : newEnvBlocksTypeMap.keySet()) {
      if (blockType.contains("air_#%#_")) continue;

      // 计算具体差值
      diff += Math.abs(newEnvBlocksTypeMap.get(blockType) / newEnvBlocksTypeMap.size() - oldEnvBlocksTypeMap.get(blockType) / oldEnvBlocksTypeMap.size());
    }
    return diff;
  }

  private static List<String> getEnvBlocksType(BlockPos[] blockPoses) {
    Minecraft mc = Minecraft.getMinecraft();

    return Arrays.stream(blockPoses).map((e) -> {
      IBlockState blockState = mc.theWorld.getBlockState(e);
      Block block = blockState.getBlock();
      return block.getRegistryName() + "_#%#_" + block.getMetaFromState(blockState);
    }).collect(Collectors.toList());
  }

  public static boolean checkEnvironmentChange() {
    return TPCheckDetector.calculateEnvironmentDiff(environment, TPCheckDetector.createEnvironmentSnapshot()) > 0.4 /*差值超过0.4*/;
  }


  public static boolean checkPositionChange() {
    Minecraft mc = Minecraft.getMinecraft();
    // 改进版位移检测（加入Y轴判断）
    double deltaX = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
    double deltaY = mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
    double deltaZ = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;

    // 三维位移阈值（根据游戏特性调整）
    double diff = Math.hypot(Math.hypot(deltaX, deltaZ), deltaY);


    return diff != 0 && (diff % 1 < 0.001D || diff % Math.sqrt(2d) < 0.001D || diff > 2.5);

  }

  public static boolean checkVelocityAnomaly() {
    Minecraft mc = Minecraft.getMinecraft();
    double predictedX = mc.thePlayer.lastTickPosX + mc.thePlayer.motionX; // 假设1tick的预期位置
    double predictedZ = mc.thePlayer.lastTickPosZ + mc.thePlayer.motionZ;
    double actualDelta = Math.hypot(mc.thePlayer.posX - predictedX, mc.thePlayer.posZ - predictedZ);


    return !(mc.thePlayer.isInLava()
        || mc.thePlayer.isPotionActive(Potion.jump)
        || mc.thePlayer.capabilities.isFlying
        || mc.thePlayer.isRiding()) && actualDelta > 0.6;
  }
}
