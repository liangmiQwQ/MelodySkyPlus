package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.libs.CustomPlayerInRange;
import net.mirolls.melodyskyplus.react.FakePlayerCheckReact;
import net.mirolls.melodyskyplus.react.NgComeReact;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.Melody.Client;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.Value;
import xyz.Melody.GUI.Notification.NotificationPublisher;
import xyz.Melody.GUI.Notification.NotificationType;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.WindowsNotification;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.modules.macros.Mining.MiningProtect;

import java.util.Arrays;
import java.util.Objects;


@SuppressWarnings("rawtypes")
@Mixin(value = xyz.Melody.module.modules.macros.Mining.MiningProtect.class, remap = false)
public abstract class MiningProtectMixin {

  public Option<Boolean> melodySkyPlus$kickOut = new Option<>("Kick him out", false);
  public Option<Boolean> melodySkyPlus$lookAt = null;

  @Shadow
  public Numbers<Double> resumeTime;

  // 构造函数
  @Shadow
  public Numbers<Double> range;
  @Shadow
  public Option<Boolean> sysNotification;
  @Shadow
  private Option<Boolean> reqSee;
  @Shadow
  private boolean niggered;
  @Shadow
  private TimerUtil resumeTimer;

  @Shadow
  protected abstract void disableMacros();

  @Shadow
  protected abstract void reEnableMacros();

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

  /**
   * @author liangmimi
   * @reason 修改IF条件语句 实现对fake players监控 实现failsafe
   */
  @Overwrite
  private void checkPause() {
    Object[] info = CustomPlayerInRange.redirectPlayerInRange(true, this.range.getValue(), this.reqSee.getValue());
    if ((Boolean) info[0]) {
      // 情况1 正常的人干扰
      if (!this.niggered) {
        EntityPlayer targetPlayer = melodySkyPlus$findPlayer((String) info[1]);

//        melodySkyPlus$warn(
//            targetPlayer != null && Objects.equals(targetPlayer.getName(),
//                // ⬆️ 用名字是否等于我来判断
//                mc.thePlayer.getName()), targetPlayer);
        melodySkyPlus$warn(
            true, targetPlayer);
      }

      this.resumeTimer.reset();
    } else if (info[2] != "NOT_THIS") {
      if (!this.niggered) {
        // 假人
        // 注册检查器,确认玩家是否飞行
        EntityPlayer targetPlayer = melodySkyPlus$findPlayer((String) info[2]);

        MelodySkyPlus.checkPlayerFlying.resetCheck();
        MelodySkyPlus.checkPlayerFlying.setPlayer(targetPlayer);
        MelodySkyPlus.checkPlayerFlying.setChecking(true);
        MelodySkyPlus.checkPlayerFlying.setCallBack(result -> {
          if (result) {
            melodySkyPlus$warn(true, targetPlayer);
          } // else: 正常假人 直接忽略

        });
      }
    } else if (!info[1].equals("NOT_THIS") && this.niggered && this.resumeTimer.hasReached(this.resumeTime.getValue() * 1000.0)) {
      // 正常算法 继续
      this.reEnableMacros();
      NotificationPublisher.queue("Mining Protect", "Macros resumed.", NotificationType.INFO, 5000);
      if (this.sysNotification.getValue()) {
        WindowsNotification.show("Mining Protect", "Macros resumed.");
      }

      this.niggered = false;
      this.resumeTimer.reset();
    }
  }

  private void melodySkyPlus$warn(boolean isMacroChecked, EntityPlayer targetPlayer) {
    Minecraft mc = Minecraft.getMinecraft();
    this.niggered = true;
    this.disableMacros();
    Client.async(() -> {
      try {
        Thread.sleep(100L);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
      } catch (Exception e) {
        MelodySkyPlus.LOGGER.error(e.getMessage());
      }
    });
    Client.warn();
    if (isMacroChecked) {
      Helper.sendMessage("[Mining Protect] Alert! Macro Check! ");
      NotificationPublisher.queue("Melody+ Failsafe", "Alert! Macro Check!", NotificationType.ERROR, 7000);
      if (this.sysNotification.getValue()) {
        WindowsNotification.show("Melody+ Failsafe", "Alert! Macro Check!");
      }

      FakePlayerCheckReact.react(targetPlayer, resumeTime.getValue());
    } else {
      Helper.sendMessage("[Mining Protect] Nigger name: " + targetPlayer.getName());
      NotificationPublisher.queue("Mining Protect", targetPlayer.getName() + " is approaching.", NotificationType.ERROR, 7000);
      if (this.sysNotification.getValue()) {
        WindowsNotification.show("Mining Protect", targetPlayer.getName() + " is approaching.");
      }
      NgComeReact.react(mc, targetPlayer, melodySkyPlus$kickOut.getValue(), melodySkyPlus$lookAt.getValue(), resumeTime.getValue(), range.getValue());
    }

  }

  private EntityPlayer melodySkyPlus$findPlayer(String playerName) {
    Minecraft mc = Minecraft.getMinecraft();

    EntityPlayer targetPlayer = null;
    if (mc.theWorld == null) {
      MelodySkyPlus.LOGGER.warn("World is null. Cannot get playerEntities.");
      return null;
    }

    for (EntityPlayer player : mc.theWorld.playerEntities) {
      if (Objects.equals(player.getName(), playerName)) {
        targetPlayer = player;
        break;
      }
    }
    return targetPlayer;
  }
}
