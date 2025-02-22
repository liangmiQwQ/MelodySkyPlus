package net.mirolls.melodyskyplus.mixin;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.modules.AutoGold;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.module.modules.macros.Mining.GoldNuker;

import java.util.Objects;

@Mixin(value = GoldNuker.class, remap = false)
public class GoldNukerMixin {
  @Inject(method = "getBlock", remap = false, at = @At("RETURN"))
  public void getBlock(CallbackInfoReturnable<BlockPos> cir) {
    if (cir.getReturnValue() == null) {
      Objects.requireNonNull(AutoGold.getINSTANCE()).findGold();
    }
  }

  @Inject(method = "destoryBlock", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;func_71038_i()V", remap = false))
  public void destoryBlock(EventPreUpdate event, CallbackInfo ci) {
    Objects.requireNonNull(AutoGold.getINSTANCE()).findGold();
  }
}
