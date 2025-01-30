package net.mirolls.melodyskyplus.mixin;

import net.mirolls.melodyskyplus.modules.Failsafe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.module.Module;

import java.util.List;

@Mixin(value = ModuleManager.class, remap = false)
public class ModuleManagerMixin {
  @Shadow
  public static List<Module> modules;

  @Inject(method = "init", remap = false, at = @At("HEAD"))
  public void init(CallbackInfo ci) {
    modules.add(new Failsafe());
  }
}
