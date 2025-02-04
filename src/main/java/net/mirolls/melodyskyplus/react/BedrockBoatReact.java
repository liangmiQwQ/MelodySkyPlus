package net.mirolls.melodyskyplus.react;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.Helper;

import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

public class BedrockBoatReact {


  public static void react(String message) {
    Helper.sendMessage("Staff checked you with bedrock boat, start to react.");

    Minecraft mc = Minecraft.getMinecraft();

    new Thread(() -> {
      long sleepTime = 5000;

      try {
        Thread.sleep((long) (sleepTime / 1.5));

        String[] messages = message.split(Pattern.quote(","));
        Random random = new Random();

        SkyblockArea mySkyblockArea = new SkyblockArea();// 这里新建而不是用Client下的原因是裤头的混淆
        mySkyblockArea.updateCurrentArea();
        Areas currentArea = mySkyblockArea.getCurrentArea();
        if (currentArea != Areas.NULL && currentArea != Areas.Dungeon_HUB && currentArea != Areas.HUB
            && currentArea != Areas.In_Dungeon) {
          mc.thePlayer.sendChatMessage(messages[random.nextInt(messages.length)].trim());
        }

        // 寻找木头位置


      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }


    }).start();
  }

  private static BlockPos findWoodBP() throws Exception {
    Minecraft mc = Minecraft.getMinecraft();

    // 先检测出4个边 然后在4个边中寻找
    // W/E:x 然后是 N/S:z
    BedrockBoat bedrockBoat = new BedrockBoat();
    for (int side = 0; side < 4; side++) {
      BlockPos testingBP = mc.thePlayer.getPosition();
      for (int i = 0; i < 9; i++) {
        switch (side) {
          case 0:
            testingBP = testingBP.east();
            break;
          case 1:
            testingBP = testingBP.west();
            break;
          case 2:
            testingBP = testingBP.north();
            break;
          case 3:
            testingBP = testingBP.south();
            break;
        }

        Block block = mc.theWorld.getBlockState(testingBP).getBlock();
        if (!Objects.equals(block.getRegistryName(), Blocks.air.getRegistryName())) {
          if (Objects.equals(block.getRegistryName(), Blocks.bedrock.getRegistryName())) {
            // 到边了
            switch (side) {
              case 0:
                bedrockBoat.setEastX(testingBP.getX());
                break;
              case 1:
                bedrockBoat.setWestX(testingBP.getX());
                break;
              case 2:
                bedrockBoat.setNorthZ(testingBP.getZ());
                break;
              case 3:
                bedrockBoat.setSouthZ(testingBP.getZ());
                break;
            }
          } else if (Objects.equals(block.getRegistryName(), Blocks.log.getRegistryName())) {
            return testingBP; // 提前找到的
          }
        }
      }
    }

    // 理论上这个时候已经获取到bedrock的四个边了 接下来比较简单了 以玩家的位置为中心 遍历4个象限

    for (int i = 0; i < 4; i++) {
      /*
        0: west north象限
        1: east north象限
        2: west south象限
        3: east south象限
       */
      // 使用算法是画四边形算法 j为正方形的边长数
      BlockPos rootBlockPos;

      if (i == 0) rootBlockPos = mc.thePlayer.getPosition().west().north();
      else if (i == 1) rootBlockPos = mc.thePlayer.getPosition().east().north();
      else if (i == 2) rootBlockPos = mc.thePlayer.getPosition().west().south();
      else rootBlockPos = mc.thePlayer.getPosition().east().south();

      for (int j = 0; j < 8 /*已经单独忽略了小正方形*/; j++) {
        // 画正方形
        BlockPos testingBlockPosX /*也就是west/east*/ = rootBlockPos;
        BlockPos testingBlockPosZ /*也就是north/south*/ = rootBlockPos;

        if (i == 0) {
          testingBlockPosX = testingBlockPosX.west();
          testingBlockPosZ = testingBlockPosZ.north();
        } else if (i == 1) {
          testingBlockPosX = testingBlockPosX.east();
          testingBlockPosZ = testingBlockPosZ.north();
        } else if (i == 2) {
          testingBlockPosX = testingBlockPosX.west();
          testingBlockPosZ = testingBlockPosZ.south();
        } else {
          testingBlockPosX = testingBlockPosX.east();
          testingBlockPosZ = testingBlockPosZ.south();
        }

        int XBlockType = getBlockType(testingBlockPosX);
        if (XBlockType == 1) {
          break;
        }
        int ZBlockType = getBlockType(testingBlockPosZ);
        if (ZBlockType == 1) {
          break;
        }

        // 加入对边界的讨论 如果马上就到边界了 那就不进行加减 也不参与后续的处理
        boolean doX = true;
        boolean doZ = true;

        if (i == 0) {
          if (bedrockBoat.getWestX() + 1 >= testingBlockPosX.getX()) {
            doX = false;
          }
          if (bedrockBoat.getNorthZ() + 1 >= testingBlockPosX.getZ()) {
            doZ = false;
          }
        } else if (i == 1) {
          if (bedrockBoat.getEastX() - 1 <= testingBlockPosX.getX()) {
            doX = false;
          }
          if (bedrockBoat.getNorthZ() + 1 >= testingBlockPosX.getZ()) {
            doZ = false;
          }
        } else if (i == 2) {
          if (bedrockBoat.getWestX() + 1 >= testingBlockPosX.getX()) {
            doX = false;
          }
          if (bedrockBoat.getNorthZ() - 1 <= testingBlockPosX.getZ()) {
            doZ = false;
          }
        } else {
          if (bedrockBoat.getEastX() - 1 <= testingBlockPosX.getX()) {
            doX = false;
          }
          if (bedrockBoat.getNorthZ() - 1 <= testingBlockPosX.getZ()) {
            doZ = false;
          }
        }

        for (int k = 0; k < j; k++) {
          if (doX) {
            int XBlockType1 = getBlockType(testingBlockPosX);
            if (XBlockType1 == 0) {
              return testingBlockPosX;
            } else if (XBlockType1 == 1) {
              break;
            }

            if (i == 0 || i == 2) {// 反向处理
              testingBlockPosX = testingBlockPosX.west();
            } else {
              testingBlockPosX = testingBlockPosX.east();
            }
          }

          if (doZ) {
            int ZBlockType1 = getBlockType(testingBlockPosZ);
            if (ZBlockType1 == 0) {
              return testingBlockPosZ;
            } else if (ZBlockType1 == 1) {
              break;
            }

            if (i == 0 || i == 1) {// 反向处理
              testingBlockPosZ = testingBlockPosZ.north();
            } else {
              testingBlockPosZ = testingBlockPosZ.south();
            }
          }
        }
      }
    }
    throw new Exception("Cannot find the wood in the bedrock boat");
  }

  private static int getBlockType(BlockPos blockPos) {
    Minecraft mc = Minecraft.getMinecraft();
    Block block = mc.theWorld.getBlockState(blockPos).getBlock();
    if (Objects.equals(block.getRegistryName(), Blocks.log.getRegistryName())) {
      return 0;
    } else if (Objects.equals(block.getRegistryName(), Blocks.bedrock.getRegistryName())) {
      return 1;
    } else {
      return 2;
    }
  }
}

class BedrockBoat {
  private int WestX;
  private int EastX;
  private int NorthZ;
  private int SouthZ;

  public int getWestX() {
    return WestX;
  }

  public void setWestX(int westX) {
    WestX = westX;
  }

  public int getEastX() {
    return EastX;
  }

  public void setEastX(int eastX) {
    EastX = eastX;
  }

  public int getNorthZ() {
    return NorthZ;
  }

  public void setNorthZ(int northZ) {
    NorthZ = northZ;
  }

  public int getSouthZ() {
    return SouthZ;
  }

  public void setSouthZ(int southZ) {
    SouthZ = southZ;
  }
}