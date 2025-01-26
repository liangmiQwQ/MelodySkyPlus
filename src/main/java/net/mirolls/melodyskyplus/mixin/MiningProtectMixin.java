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
import xyz.Melody.Utils.math.RotationUtil;

import java.util.Objects;

@Mixin(value = xyz.Melody.module.modules.macros.Mining.MiningProtect.class, remap = false)
public class MiningProtectMixin {
//  public Option<Boolean> lookAt = new Option<>("Look at him", true);

  @Shadow
  private boolean niggered;


  @Inject(method = "checkPause",
      at = @At(value = "INVOKE", target = "Lxyz/Melody/Utils/AFKUtils;playerInRange(DZ)[Ljava/lang/Object;")
      , locals = LocalCapture.CAPTURE_FAILSOFT
  )

  public void checkPause(CallbackInfo ci, Object[] info) {
    MelodySkyPlus.LOGGER.info("触发了Mixin方法");
    if ((Boolean) info[0]) {
      if (!this.niggered) {
        String niggerName = (String) info[1];
        EntityPlayer targetPlayer = null;
        Minecraft mc = Minecraft.getMinecraft();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
          if (Objects.equals(player.getName(), niggerName)) {
            targetPlayer = player;
          }
        }

        if (targetPlayer != null) {
          BlockPos rotationTarget = targetPlayer.playerLocation;
          RotationUtil.posToRotation(rotationTarget);
        } else {
          MelodySkyPlus.LOGGER.warn("Cannot found player " + niggerName);
        }
      }
    }
  }
}
