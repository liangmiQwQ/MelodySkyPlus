package net.mirolls.melodyskyplus.react;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.math.Rotation;

import java.util.Random;

@SuppressWarnings("BusyWait")
public class FakePlayerCheckReact {

  public static void react(EntityPlayer fakePlayer, double resumeTime) {
    Minecraft mc = Minecraft.getMinecraft();

    new Thread(() -> {
      long sleepTime = (long) (resumeTime * 500);

      try {
        Thread.sleep(sleepTime / 2);

        MelodySkyPlus.rotationLib.setSpeedCoefficient(2.0F);
        MelodySkyPlus.rotationLib.startRotating();
        MelodySkyPlus.rotationLib.setTargetRotation(new Rotation(mc.thePlayer.rotationYaw, -90F));

        Thread.sleep((long) (sleepTime / 1.5));

        Random random = new Random();
        sendQuestionMessage(random, mc);

        Thread.sleep(sleepTime / 5);

        rotate(mc, fakePlayer, sleepTime, random);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
  }

  private static void rotate(Minecraft mc, EntityPlayer fakePlayer, long sleepTime, Random random) throws InterruptedException {
    // 跳起来 瞎转
    int needReactTimes = 5;
    int reactTime = 0;
    while (MathUtil.distanceToEntity(mc.thePlayer, fakePlayer) < 50) {
      if (reactTime < 6) {
        int rotatingMode = random.nextInt(4);

        if (rotatingMode == 0) {
          // 多重复合型
          Thread.sleep(sleepTime / needReactTimes / 8);
          if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
          }
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
          MelodySkyPlus.rotationLib.setSpeedCoefficient(random.nextFloat() + 1);
          MelodySkyPlus.rotationLib.startRotating();
          Rotation rotation1 = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
          MelodySkyPlus.rotationLib.setTargetRotation(rotation1);

          Thread.sleep(sleepTime / needReactTimes / 8);
          boolean moreSneak = random.nextBoolean();
          if (moreSneak) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
          }
          Thread.sleep(sleepTime / needReactTimes / 8);
          if (moreSneak) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
          }

          Thread.sleep(sleepTime / needReactTimes / 8);
          if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
          }
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

          Thread.sleep(sleepTime / needReactTimes / 8);
          if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
          }
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
          MelodySkyPlus.rotationLib.setSpeedCoefficient(random.nextFloat() + 1);
          MelodySkyPlus.rotationLib.startRotating();
          Rotation rotation2 = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
          MelodySkyPlus.rotationLib.setTargetRotation(rotation2);

          Thread.sleep(sleepTime / needReactTimes / 8);
          boolean moreSneak1 = random.nextBoolean();
          if (moreSneak1) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
          }
          Thread.sleep(sleepTime / needReactTimes / 8);
          if (moreSneak1) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
          }

          Thread.sleep(sleepTime / needReactTimes / 8);
          if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
          }
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        } else if (rotatingMode == 1) {
          // 狂风暴雨形
          for (int i = 0; i < 16; i++) {
            Thread.sleep(sleepTime / needReactTimes / 32);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            Rotation rotation = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
            MelodySkyPlus.rotationLib.setTargetRotation(rotation);

            Thread.sleep(sleepTime / needReactTimes / 32);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
          }
        } else if (rotatingMode == 2) {
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);

          // 和风细雨型
          for (int i = 0; i < 2; i++) {
            Rotation rotation = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
            MelodySkyPlus.rotationLib.setTargetRotation(rotation);
            Thread.sleep(sleepTime / needReactTimes / 2);
          }

          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        } else {
          // 瞎胡乱动型
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
          for (int i = 0; i < 4; i++) {
            int walkMode = random.nextInt(8);

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

            Thread.sleep(sleepTime / needReactTimes / 4);

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
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
      } else {
        break;
      }
      reactTime++;
    }
  }

  private static void sendQuestionMessage(Random random, Minecraft mc) {
    String[] replyMessage = new String[]{
        // 随便质疑型
        "wtf?",
        "???",
        "????",
        "wtf???",
        "?",
        "t??",
        "w?",

        // 核心质疑型
        "mining trigger killaura??",
        "why killaura check when mining hyp",
        "is there macro check?",

        // 假人位置特化
        "why fake me flying??",
        "dupe above me?",
        "why clone mining??",

        // 反向逻辑质问
        "how killaura while mining?",
        "i was macro checked lol",
        "omg there is a staff",

        // 管理测试猜测
        "admin marco checking?",
        "? staff checking",
        "are admins in lobby?",
        "staff in lobby?"
    };


    mc.thePlayer.sendChatMessage(replyMessage[random.nextInt(replyMessage.length)]);
  }
}
