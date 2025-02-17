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
    int radius = 6;

    List<BlockPos> blocks = new ArrayList<>();
    for (int y = -radius / 2; y <= radius / 2; y++) {
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
      Integer newEBTMap = newEnvBlocksTypeMap.get(blockType);
      if (newEBTMap == null) newEBTMap = 0;

      Integer oldEBTMap = newEnvBlocksTypeMap.get(blockType);
      if (oldEBTMap == null) oldEBTMap = 0;

      diff += Math.abs(
          newEBTMap / newEnvBlocksTypeMap.size() - oldEBTMap / oldEnvBlocksTypeMap.size());
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

  public static int checkEnvironmentChange() {
    if (Minecraft.getMinecraft().thePlayer.fallDistance > 0.5) return 0;// 你都摔落了你还瞎扯什么玩意
    int returnValue = (int) Math.floor(TPCheckDetector.calculateEnvironmentDiff(environment, TPCheckDetector.createEnvironmentSnapshot())) / 10;
    if (returnValue > 10) {
      returnValue = 8;
    }
    return returnValue;
  }


  public static int checkPositionChange() {
    Minecraft mc = Minecraft.getMinecraft();
    // 改进版位移检测（加入Y轴判断）
    double deltaX = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
    double deltaY = mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
    double deltaZ = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;

    // 三维位移阈值（根据游戏特性调整）
    double diff = Math.hypot(Math.hypot(deltaX, deltaZ), deltaY / 5 /*只是检测 但是不把你当回事*/);


    if (diff >= 7) {
      return 20;
    }
    if (diff >= 5) {
      return 5;
    }
    if (diff != 0 && (diff % 1 < 0.001D || diff % Math.sqrt(2d) < 0.001D || diff > 2.5)) {
      return 4;
    }
    if (diff != 0 && (diff % 0.1 < 0.001D)) return 3;
    if (diff > 0.7) return 1;

    return 0;
  }

  public static int checkMotion() {
    Minecraft mc = Minecraft.getMinecraft();

    int warnLevel = 0;

    if (Math.abs(mc.thePlayer.motionX - 0) < 0.0001d /*相当于Motion = 0*/) {
      warnLevel += 2;
    }
    if (Math.abs(mc.thePlayer.motionZ - 0) < 0.0001d /*相当于Motion = 0*/) {
      warnLevel += 2;
    }
    if (warnLevel == 4) {
      warnLevel = 18;
    }
    return warnLevel;
  }

  public static int checkVelocity() {
    Minecraft mc = Minecraft.getMinecraft();

    double predictedX = mc.thePlayer.lastTickPosX + mc.thePlayer.motionX; // 假设1tick的预期位置
    double predictedZ = mc.thePlayer.lastTickPosZ + mc.thePlayer.motionZ;
    double actualDelta = Math.hypot(mc.thePlayer.posX - predictedX, mc.thePlayer.posZ - predictedZ);

    if (!(mc.thePlayer.isInLava()
        || mc.thePlayer.isPotionActive(Potion.jump)
        || mc.thePlayer.capabilities.isFlying
        || mc.thePlayer.isRiding())) {
      if (actualDelta > 0.6) {
        if (mc.thePlayer.onGround) {
          return 3;
        } else {
          return 2;
        }
      }
    }
    return 0;
  }
}
