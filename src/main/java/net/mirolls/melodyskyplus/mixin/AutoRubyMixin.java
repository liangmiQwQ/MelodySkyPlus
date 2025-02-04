package net.mirolls.melodyskyplus.mixin;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.modules.Failsafe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;

import java.util.Objects;

@Mixin(value = AutoRuby.class, remap = false)
public class AutoRubyMixin {
  @Shadow
  private boolean etherWarped;

  @Inject(method = "etherWarp", at = @At(value = "HEAD"), remap = false)
  private void etherWarp(BlockPos pos, CallbackInfo ci) {
    if (!this.etherWarped) {
      Objects.requireNonNull(Failsafe.getINSTANCE()).lastLegitTeleport = Failsafe.getINSTANCE().nowTick;
    }
  }
}
