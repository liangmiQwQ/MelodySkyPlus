package net.mirolls.melodyskyplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = xyz.Melody.module.modules.macros.Mining.MiningProtect.class, remap = false)
public class MiningProtectMixin {
//  public Option<Boolean> lookAt = new Option<>("Look at him", true);

  @Shadow
  private boolean niggered;

  @Inject(method = "disableMacros", at = @At("HEAD"))
  public void disableMacros(CallbackInfo ci) {

  }

//  @Inject(method = "checkPause",
//      at = @At(value = "INVOKE", target = "Lxyz/Melody/Utils/AFKUtils;playerInRange(DZ)[Ljava/lang/Object;")
//      , locals = LocalCapture.CAPTURE_FAILSOFT
//  )
//
//  public void checkPause(CallbackInfo ci, Object[] info) {
//    MelodySkyPlus.LOGGER.info("你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好");
//  }
//    if ((Boolean) info[0]) {
//      if (!this.niggered) {
//        String niggerName = (String) info[1];
//        EntityPlayer targetPlayer = null;
//        Minecraft mc = Minecraft.getMinecraft();
//
//        for (EntityPlayer player : mc.theWorld.playerEntities) {
//          if (Objects.equals(player.getName(), niggerName)) {
//            targetPlayer = player;
//          }
//        }
//
//        if (targetPlayer != null) {
//          BlockPos rotationTarget = targetPlayer.playerLocation;
//          RotationUtil.posToRotation(rotationTarget);
//        } else {
//          MelodySkyPlus.LOGGER.warn("Cannot found player " + niggerName);
//        }
//      }
//    }
//  }
}
