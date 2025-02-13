package net.mirolls.melodyskyplus.mixin;

import net.mirolls.melodyskyplus.client.AntiBug;
import net.mirolls.melodyskyplus.gui.GemstoneTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.GUI.Hud.HUDElement;
import xyz.Melody.GUI.Hud.HUDManager;

@Mixin(value = HUDManager.class, remap = false)
public abstract class HUDManagerMixin {

  @Shadow
  protected abstract void addElement(HUDElement e);

  @Inject(method = "init", remap = false, at = @At("HEAD"))
  public void init(CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      addElement(new GemstoneTick());
    }
  }
}
