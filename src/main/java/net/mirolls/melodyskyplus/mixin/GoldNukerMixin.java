package net.mirolls.melodyskyplus.mixin;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.client.AntiBug;
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
  private int lastSwingHandTick;
  private int nowTick;

  @Inject(method = "getBlock", remap = false, at = @At("RETURN"))
  public void getBlock(CallbackInfoReturnable<BlockPos> cir) {
    if (AntiBug.isBugRemoved()) {
      if (cir.getReturnValue() == null) {
        Objects.requireNonNull(AutoGold.getINSTANCE()).findGold();
      }
    }
  }

  @Inject(method = "destoryBlock", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;func_71038_i()V", remap = false))
  public void destoryBlock(EventPreUpdate event, CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      lastSwingHandTick = nowTick;
    }
  }

  @Inject(method = "destoryBlock", remap = false, at = @At("HEAD"))
  public void onTick(EventPreUpdate event, CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      nowTick++;

      if (nowTick - lastSwingHandTick > 20) {
        // 超过1秒没挖了
        Objects.requireNonNull(AutoGold.getINSTANCE()).findGold();
      }
    }
  }

  @Inject(method = "onEnable", remap = false, at = @At("HEAD"))
  public void onEnable(CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {


      nowTick = 0;
      lastSwingHandTick = 0;
    }
  }
}
