package net.mirolls.melodyskyplus.mixin;

import net.mirolls.melodyskyplus.gui.GemstoneTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.GUI.CustomUI.HUDApi;
import xyz.Melody.GUI.CustomUI.HUDManager;

import java.util.List;

@Mixin(value = HUDManager.class, remap = false)
public class HUDManagerMixin {
  @Shadow
  public static List<HUDApi> apis;

  @Inject(method = "init", remap = false, at = @At("HEAD"))
  public void init(CallbackInfo ci) {
    apis.add(new GemstoneTick());
  }
}
