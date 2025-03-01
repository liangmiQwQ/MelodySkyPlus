package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.modules.Failsafe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.Value;
import xyz.Melody.Utils.game.ScoreboardUtils;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;
import xyz.Melody.module.modules.macros.Mining.GemstoneNuker;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
@Mixin(value = AutoRuby.class, remap = false)
public class AutoRubyMixin {
  private final Numbers<Double> melodySkyPlus$maxHeat = new Numbers<>("MaxHeat", 95.0, 1.0, 100.0, 1.0);
  private final Numbers<Double> melodySkyPlus$minHeat = new Numbers<>("MinHeat", 95.0, 1.0, 100.0, 1.0);
  @Shadow
  public boolean started;
  @Shadow
  private TimerUtil ewTimer;
  private boolean jumping = false;
  private Option<Boolean> melodySkyPlus$autoHeat = null;

  @ModifyArg(method = "<init>", remap = false, at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/AutoRuby;addValues([Lxyz/Melody/Event/value/Value;)V"))
  private Value[] init(Value[] originalValues) {
    melodySkyPlus$autoHeat = new Option<>("AutoHeat", false, val -> {
      if (AutoRuby.getINSTANCE() != null) {
        melodySkyPlus$maxHeat.setEnabled(val);
        melodySkyPlus$minHeat.setEnabled(val);
      }
    });

    Value[] returnValues = Arrays.copyOf(originalValues, originalValues.length + 3);

    returnValues[returnValues.length - 3] = melodySkyPlus$autoHeat;
    returnValues[returnValues.length - 2] = melodySkyPlus$minHeat;
    returnValues[returnValues.length - 1] = melodySkyPlus$maxHeat;

    return returnValues;
  }

  @Inject(method = "onEnable", at = @At("HEAD"))
  private void onEnable(CallbackInfo ci) {
    jumping = false;
  }

  @Inject(method = "idk", at = @At("HEAD"), remap = false)
  private void idk(EventTick event, CallbackInfo ci) {
    Minecraft mc = Minecraft.getMinecraft();

    if (this.ewTimer.hasReached(0)) {
      Objects.requireNonNull(Failsafe.getINSTANCE()).lastLegitTeleport = Failsafe.getINSTANCE().nowTick;
    }

    if (melodySkyPlus$autoHeat.getValue()) {
      GemstoneNuker gsn = GemstoneNuker.getINSTANCE();
      if (gsn.isEnabled() && this.started) {
        List<String> scoreBoard = ScoreboardUtils.getScoreboard();
        for (String line : scoreBoard) {
          if (line.toLowerCase().contains("heat:")) {
            if (mc.thePlayer.posY <= 64 && mc.thePlayer.posY >= 64 - 6) {
              int heat = melodySkyPlus$getHeat(line.replaceAll(".*Heat: §[a-f0-9]", ""));
              if (heat >= melodySkyPlus$maxHeat.getValue() && !jumping) {
                jumping = true;
                MelodySkyPlus.rotationLib.setCallBack(() -> {
                  KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                });
                MelodySkyPlus.rotationLib.setTargetRotation(new Rotation(mc.thePlayer.rotationYaw, -90F));
                MelodySkyPlus.rotationLib.setSpeedCoefficient(2F);
                MelodySkyPlus.rotationLib.startRotating();
                if (gsn.isEnabled()) {
                  gsn.setEnabled(false);
                }
              }
              if (melodySkyPlus$minHeat.getValue() >= heat && jumping) {
                jumping = false;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                new Thread(() -> {
                  try {
                    Thread.sleep(1000);
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                  if (!gsn.isEnabled()) {
                    gsn.setEnabled(true);
                  }
                }).start();
              }
            }
            break;
          }
        }
      }
    }

    if (mc.thePlayer.onGround && jumping) {
      mc.thePlayer.jump();
    }
  }

  private int melodySkyPlus$getHeat(String input) {
    Pattern pattern = Pattern.compile("^\\d+");
    Matcher matcher = pattern.matcher(input.trim());

    if (matcher.find()) {
      return Integer.parseInt(matcher.group());
    } else {
      throw new IllegalArgumentException("Error: No target heat found");
    }
  }
}
