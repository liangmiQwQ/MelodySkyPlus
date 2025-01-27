package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.Value;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;
import xyz.Melody.module.modules.macros.Mining.MiningProtect;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;


@SuppressWarnings("rawtypes")
@Mixin(value = xyz.Melody.module.modules.macros.Mining.MiningProtect.class, remap = false)
public class MiningProtectMixin {

  public Option<Boolean> melodySkyPlus$kickOut = new Option<>("Kick him out", true);
  public Option<Boolean> melodySkyPlus$lookAt = null;

  @Shadow
  public Numbers<Double> resumeTime;


  // 构造函数

  @ModifyArg(method = "<init>",
      at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/MiningProtect;addValues([Lxyz/Melody/Event/value/Value;)V", remap = false),
      index = 0)
  public Value[] addValueArgs(Value[] originalValues) {
    // 先初始化一下 避免mixin错误 在此初始化的原因: https://github.com/SpongePowered/Mixin/issues/322
    Option<Boolean> lobby = new Option<>("/lobby", false,
        value -> {
          MiningProtect instance = MiningProtect.getINSTANCE();
          if (instance != null) {
            // 两个小弟
            instance.escapeRange.setEnabled(value);
            instance.escapeTime.setEnabled(value);

            // 互相不支持的2个项目
            this.melodySkyPlus$lookAt.setEnabled(!value);
            this.melodySkyPlus$kickOut.setEnabled(!value);

            instance.lobby.setValue(value); // 反映到源值 允许代码继续运行
          }
        }
    );

    melodySkyPlus$lookAt = new Option<>("Look at him", true,
        value -> {
          MiningProtect instance = MiningProtect.getINSTANCE();
          if (instance != null) {
            // 小弟
            this.melodySkyPlus$kickOut.setEnabled(value);

            // 互不支持的项目
            lobby.setEnabled(!value);
            instance.escapeRange.setEnabled(!value);
            instance.escapeTime.setEnabled(!value);
          }
        }
    );


    Value[] returnValues = Arrays.copyOf(originalValues, originalValues.length + 2);

    returnValues[returnValues.length - 2] = melodySkyPlus$lookAt;
    returnValues[returnValues.length - 1] = melodySkyPlus$kickOut;
    returnValues[1] = lobby;

    return returnValues;
  }

  @Inject(method = "checkPause",
      at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/MiningProtect;disableMacros()V", remap = false)
      , locals = LocalCapture.CAPTURE_FAILSOFT
  )
  public void checkPause(CallbackInfo ci, Object[] info) {
    if (melodySkyPlus$lookAt.getValue()) {
      MelodySkyPlus.LOGGER.info("触发了checkPause 并且需要做出反馈");

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
        if (melodySkyPlus$kickOut.getValue()) {
          EntityPlayer newPlayer = targetPlayer;
          new Thread(() -> {
            try {
              long sleepTime = (long) (resumeTime.getValue() * 500);
              Thread.sleep(sleepTime/*等待一半的时间*/);

              BlockPos newPlayerLocation = newPlayer.getPosition();

              if (newPlayerLocation != null) {
                if (MathUtil.distanceToPos(newPlayerLocation, playerLocation)
                    < (float) (sleepTime * 5) / 1000 /*我这边算他1s走5m*/
                    || MathUtil.distanceToPos(mc.thePlayer.getPosition(), newPlayerLocation) < 5) {
                  // 乌龟速度 来打扰的
                  String[] replyMessage = new String[]{
                      "hi? im first here.",
                      "I was here first, pls leave.",
                      "Excuse me, im first here",
                      "sry but im first here",
                      "hi?",
                      "?",
                      "hello?",
                      "hello? im' here first",
                      "can you leave? im first here",
                      "hey?",
                      "hey, im frist here",
                      "please leave bro im first here",
                      "bro? im already here",
                      "umm, this is my spot?",
                      "hello? can u pls leave?",
                      "this is mine, pls go away.",
                      "excuse me? what r u doing?",
                      "bruh, im mining here.",
                      "u lost or smth? this is mine.",
                      "hi? why r u here?",
                      "could u pls leave? im first",
                      "dude this is my area pls leave",
                      "huh? i was here first bro",
                      "sry but i was mining first",
                      "pls leave bro this is mine",
                      "w?",
                      "a?",
                      "emm"
                  };

                  mc.thePlayer.sendChatMessage(replyMessage[new Random().nextInt(replyMessage.length)]);

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
