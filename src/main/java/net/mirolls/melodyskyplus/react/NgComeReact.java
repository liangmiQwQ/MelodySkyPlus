package net.mirolls.melodyskyplus.react;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

public class NgComeReact {
  public static void react(
      Minecraft mc,
      EntityPlayer targetPlayer,
      boolean kickOut,
      boolean lookAt,
      double resumeTime,
      double range) {
    if (lookAt) {
      if (targetPlayer != null) {

        BlockPos playerLocation = targetPlayer.getPosition();
        if (playerLocation == null) {
          MelodySkyPlus.LOGGER.warn(
              "Player " + targetPlayer.getName() + " exists but #getPosition() is null.");
          return;
        }
        MelodySkyPlus.LOGGER.info(
            "Found Player "
                + targetPlayer.getName()
                + " Ready to rotate to him."
                + "His "
                + playerLocation.toString());

        Rotation rotation = RotationUtil.posToRotation(playerLocation);

        MelodySkyPlus.rotationLib.setTargetRotation(rotation);
        MelodySkyPlus.rotationLib.startRotating();
        MelodySkyPlus.rotationLib.setSpeedCoefficient(5.0F);

        // 创建一个Thread 如果过了一段时间这b东西还没走就进行驱逐
        if (kickOut) {
          new Thread(
                  () -> {
                    try {
                      long sleepTime = (long) (resumeTime * 500);
                      Thread.sleep(sleepTime /*等待一半的时间*/);

                      BlockPos newPlayerLocation = targetPlayer.getPosition();
                      float distanceToMe =
                          MathUtil.distanceToPos(mc.thePlayer.getPosition(), newPlayerLocation);

                      if (MathUtil.distanceToPos(newPlayerLocation, playerLocation)
                              < (float) (sleepTime * 5) / 1000 /*我这边算他1s走5m*/
                          || distanceToMe < 5) {
                        if (distanceToMe < range) {
                          // 乌龟速度 或 来打扰的
                          Rotation newRotation = RotationUtil.posToRotation(newPlayerLocation);
                          MelodySkyPlus.rotationLib.setTargetRotation(newRotation);
                          MelodySkyPlus.rotationLib.startRotating();
                          MelodySkyPlus.rotationLib.setSpeedCoefficient(1.0F);

                          Thread.sleep(sleepTime / 2 /*等待1/4的时间*/);

                          String[] replyMessage =
                              new String[] {
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

                          SkyblockArea mySkyblockArea =
                              new SkyblockArea(); // 这里新建而不是用Client下的原因是裤头的混淆
                          mySkyblockArea.updateCurrentArea();
                          Areas currentArea = mySkyblockArea.getCurrentArea();

                          if (currentArea != Areas.NULL
                              && currentArea != Areas.Dungeon_HUB
                              && currentArea != Areas.HUB
                              && currentArea != Areas.In_Dungeon) {
                            mc.thePlayer.sendChatMessage(
                                "/ac " + replyMessage[new Random().nextInt(replyMessage.length)]);
                          }
                        }
                      } else {
                        MelodySkyPlus.LOGGER.warn(
                            "Player "
                                + targetPlayer.getName()
                                + "'s #getPosition() is null. Maybe he's leave");
                      }
                    } catch (InterruptedException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .start();
        }
      } else {
        MelodySkyPlus.LOGGER.warn("Cannot found player (Null)");
      }
    }
  }
}
