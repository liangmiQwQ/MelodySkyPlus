package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.Objects;

@Mixin(value = xyz.Melody.module.modules.macros.Mining.MiningProtect.class, remap = false)
public class MiningProtectMixin {
  public Option<Boolean> lookAt = new Option<>("Look at him", true);

  @Shadow
  private boolean niggered;


  @Inject(method = "checkPause",
      at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/MiningProtect;disableMacros()V")
//      at = @At("RETURN")
      , locals = LocalCapture.CAPTURE_FAILSOFT
  )
  public void checkPause(CallbackInfo ci, Object[] info) {
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

      BlockPos rotationTarget = targetPlayer.getPosition();
      if (rotationTarget == null) {
        MelodySkyPlus.LOGGER.warn("Player " + niggerName + " exists but #getPosition() is null.");
        return;
      }
      MelodySkyPlus.LOGGER.info("Found Player " + niggerName + " Ready to rotate to him." + "His " + rotationTarget.toString());
      Rotation rotation = RotationUtil.posToRotation(rotationTarget);

//      RotationUtil.silentLook(rotation.getYaw(), rotation.getPitch(), null);
      MelodySkyPlus.rotationLib.setTargetRotation(rotation);
      MelodySkyPlus.rotationLib.setRotating(true);
    } else {
      MelodySkyPlus.LOGGER.warn("Cannot found player " + niggerName);
    }
  }


  // 构造函数 新增属性
//  @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/MiningProtect;addValues([Lxyz/Melody/Event/value/Value;)V"))
//  public Value[] addValueArgs(Value[] originalValues) {
//    Value[] returnValues = new Value[originalValues.length + 1];
//
//    returnValues[returnValues.length - 1] = lookAt;
//    return returnValues;
//  }
}
