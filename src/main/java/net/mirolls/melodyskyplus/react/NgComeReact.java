package net.mirolls.melodyskyplus.react;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.Objects;
import java.util.Random;

public class NgComeReact {
  public static void react(Object[] info, boolean lookAt, boolean kickOut, double resumeTime, double range) {
    if (lookAt) {

      if (info == null || info.length < 2 || !(info[1] instanceof String)) {
        MelodySkyPlus.LOGGER.warn("Info array is invalid or missing the player name.");
        return;
      }
      String niggerName = (String) info[1];
      EntityPlayer targetPlayer = null;
      Minecraft mc = Minecraft.getMinecraft();

      if (mc.theWorld == null) {
        MelodySkyPlus.LOGGER.warn("World is null. Cannot get playerEntities.");
        return;
      }

      for (EntityPlayer player : mc.theWorld.playerEntities) {
        if (Objects.equals(player.getName(), niggerName)) {
          targetPlayer = player;
          break;
        }
      }

      if (targetPlayer != null) {

        BlockPos playerLocation = targetPlayer.getPosition();
        if (playerLocation == null) {
          MelodySkyPlus.LOGGER.warn("Player " + niggerName + " exists but #getPosition() is null.");
          return;
        }
        MelodySkyPlus.LOGGER.info("Found Player " + niggerName + " Ready to rotate to him." + "His " + playerLocation.toString());
        Rotation rotation = RotationUtil.posToRotation(playerLocation);

        MelodySkyPlus.rotationLib.setTargetRotation(rotation);
        MelodySkyPlus.rotationLib.setRotating(true);
        MelodySkyPlus.rotationLib.setSpeedCoefficient(5.0F);


        // 创建一个Thread 如果过了一段时间这b东西还没走就进行驱逐
        if (kickOut) {
          EntityPlayer newPlayer = targetPlayer;
          new Thread(() -> {
            try {
              long sleepTime = (long) (resumeTime * 500);
              Thread.sleep(sleepTime/*等待一半的时间*/);


              BlockPos newPlayerLocation = newPlayer.getPosition();

              if (newPlayerLocation != null) {
                float distanceToMe = MathUtil.distanceToPos(mc.thePlayer.getPosition(), newPlayerLocation);
                if (MathUtil.distanceToPos(newPlayerLocation, playerLocation)
                    < (float) (sleepTime * 5) / 1000 /*我这边算他1s走5m*/
                    || distanceToMe < 5) {
                  if (distanceToMe < range) {
                    // 乌龟速度 或 来打扰的
                    String[] replyMessage = new String[]{
                        "hi?",
                        "?",
                        "hello?",
                        "w?",
                        "a?",
                        "emm",
                        "hey?",
                        "hey, im frist here",
                        "hello? im' here first",
                        "hi? im first here.",
                        "hi?",
                        "?",
                        "hello?",
                        "w?",
                        "a?",
                        "emm",
                        "hey?",
                        "hey, im frist here",
                        "hello? im' here first",
                        "hi? im first here.",
                        // 标准语句需要更大的概率
                        "can you leave? im first here",
                        "please leave bro im first here",
                        "bro? im already here",
                        "umm, this is my spot?",
                        "I was here first, pls leave.",
                        "Excuse me, im first here",
                        "sry but im first here",
                        "hello? can u pls leave?",
                        "this is mine, pls go away.",
                        "excuse me? what ru doing?",
                        "bruh, im mining here.",
                        "could u pls leave? im first",
                        "huh? i was here first bro",
                        "sry but i was mining first",
                        "pls leave bro this is mine",
                    };

                    mc.thePlayer.sendChatMessage(replyMessage[new Random().nextInt(replyMessage.length)]);
                  }
                }
              } else {
                MelodySkyPlus.LOGGER.warn("Player " + niggerName + "'s #getPosition() is null. Maybe he's leave");
              }
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }).start();
        }
      } else {
        MelodySkyPlus.LOGGER.warn("Cannot found player " + niggerName);
      }
    }
  }
}
