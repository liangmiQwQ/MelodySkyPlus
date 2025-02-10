package net.mirolls.melodyskyplus.react.failsafe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.math.Rotation;

import java.util.Random;
import java.util.regex.Pattern;

public class React {
  public static void rotate(Minecraft mc, Run run, long sleepTime, Random random) throws InterruptedException {
    // 跳起来 瞎转
    int needReactTimes = 5;

    for (int reactTime = 0; reactTime < 10; reactTime++) {
//    while (MathUtil.distanceToEntity(mc.thePlayer, fakePlayer) < 50) {
      if (run.canRun()) {
        int rotatingMode = random.nextInt(4);

        if (rotatingMode == 0) {
          // 多重复合型
          Thread.sleep(sleepTime / needReactTimes / 8);
          if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
          }
          if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
          }
          MelodySkyPlus.rotationLib.setSpeedCoefficient(random.nextFloat() + 1);
          MelodySkyPlus.rotationLib.startRotating();
          Rotation rotation1 = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
          MelodySkyPlus.rotationLib.setTargetRotation(rotation1);

          Thread.sleep(sleepTime / needReactTimes / 8);
          boolean moreSneak = random.nextBoolean();
          if (moreSneak) {
            if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
              KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }
          }
          Thread.sleep(sleepTime / needReactTimes / 8);
          if (moreSneak) {
            if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {

              KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            }
          }

          Thread.sleep(sleepTime / needReactTimes / 8);
          if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
          }
          if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
          }

          Thread.sleep(sleepTime / needReactTimes / 8);
          if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
          }
          if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
          }
          MelodySkyPlus.rotationLib.setSpeedCoefficient(random.nextFloat() + 1);
          MelodySkyPlus.rotationLib.startRotating();
          Rotation rotation2 = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
          MelodySkyPlus.rotationLib.setTargetRotation(rotation2);

          Thread.sleep(sleepTime / needReactTimes / 8);
          boolean moreSneak1 = random.nextBoolean();
          if (moreSneak1) {
            if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
              KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }
          }
          Thread.sleep(sleepTime / needReactTimes / 8);
          if (moreSneak1) {
            if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
              KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            }
          }

          Thread.sleep(sleepTime / needReactTimes / 8);
          if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
          }
          if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
          }
        } else if (rotatingMode == 1) {
          // 狂风暴雨形
          for (int i = 0; i < 16; i++) {
            Thread.sleep(sleepTime / needReactTimes / 32);
            if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
              KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            }
            Rotation rotation = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
            MelodySkyPlus.rotationLib.setTargetRotation(rotation);

            Thread.sleep(sleepTime / needReactTimes / 32);
            if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
              KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }
          }
        } else if (rotatingMode == 2) {
          if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
          }

          // 和风细雨型
          for (int i = 0; i < 2; i++) {
            Rotation rotation = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
            MelodySkyPlus.rotationLib.setTargetRotation(rotation);
            Thread.sleep(sleepTime / needReactTimes / 2);
          }

          if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
          }
        } else {
          // 瞎胡乱动型
          if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
          }
          for (int i = 0; i < 4; i++) {
            int walkMode = random.nextInt(8);

            if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
              if (walkMode == 0) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
              } else if (walkMode == 1) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
              } else if (walkMode == 2) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
              } else if (walkMode == 3) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
              } else if (walkMode == 4) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
              } else if (walkMode == 5) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
              } else if (walkMode == 6) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
              } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
              }
            }

            Thread.sleep(sleepTime / needReactTimes / 4);

            if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {

              if (walkMode == 0) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
              } else if (walkMode == 1) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
              } else if (walkMode == 2) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
              } else if (walkMode == 3) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
              } else if (walkMode == 4) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
              } else if (walkMode == 5) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
              } else if (walkMode == 6) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
              } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
              }
            }

          }
          if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
          }
        }
      } else {
        break;
      }
      reactTime++;
    }
  }

  public static void sendQuestionMessage(Random random, Minecraft mc, String fakePlayerCheckMessage) {
    String[] replyMessage = fakePlayerCheckMessage.split(Pattern.quote(","));


    SkyblockArea mySkyblockArea = new SkyblockArea();// 这里新建而不是用Client下的原因是裤头的混淆
    mySkyblockArea.updateCurrentArea();
    Areas currentArea = mySkyblockArea.getCurrentArea();
    if (currentArea != Areas.NULL && currentArea != Areas.Dungeon_HUB && currentArea != Areas.HUB
        && currentArea != Areas.In_Dungeon) {
      if (ModuleManager.getModuleByName("Failsafe").isEnabled()) {
        mc.thePlayer.sendChatMessage(replyMessage[random.nextInt(replyMessage.length)].trim());
      }
    }
  }

  public static void drawBigTitle(String text) {

  }
}
