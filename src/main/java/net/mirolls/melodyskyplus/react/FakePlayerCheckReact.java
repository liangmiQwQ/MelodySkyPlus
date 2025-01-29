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


        Random random = new Random();
        mc.thePlayer.sendChatMessage(replyMessage[random.nextInt(replyMessage.length)]);

        Thread.sleep(sleepTime / 5);

        // 跳起来 瞎转
        int needReactTimes = 5;
        int reactTime = 0;
        while (MathUtil.distanceToEntity(mc.thePlayer, fakePlayer) < 50) {
          if (reactTime < 6) {
            Thread.sleep(sleepTime / needReactTimes / 4);
            if (mc.thePlayer.onGround) {
              mc.thePlayer.jump();
            }
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            MelodySkyPlus.rotationLib.setSpeedCoefficient(random.nextFloat() + 1);
            MelodySkyPlus.rotationLib.startRotating();
            Rotation rotation1 = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
            MelodySkyPlus.rotationLib.setTargetRotation(rotation1);

            Thread.sleep(sleepTime / needReactTimes / 4);
            if (mc.thePlayer.onGround) {
              mc.thePlayer.jump();
            }
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

            Thread.sleep(sleepTime / needReactTimes / 4);
            if (mc.thePlayer.onGround) {
              mc.thePlayer.jump();
            }
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            MelodySkyPlus.rotationLib.setSpeedCoefficient(random.nextFloat() + 1);
            MelodySkyPlus.rotationLib.startRotating();
            Rotation rotation2 = new Rotation(random.nextFloat() + random.nextInt(179), (random.nextBoolean() ? 1 : -1) * (random.nextFloat() + random.nextInt(89)));
            MelodySkyPlus.rotationLib.setTargetRotation(rotation2);

            Thread.sleep(sleepTime / needReactTimes / 4);
            if (mc.thePlayer.onGround) {
              mc.thePlayer.jump();
            }
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
          } else {
            break;
          }
          reactTime++;
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
  }
}
