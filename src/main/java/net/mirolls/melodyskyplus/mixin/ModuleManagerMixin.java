package net.mirolls.melodyskyplus.mixin;

import java.util.List;
import java.util.Objects;
import net.mirolls.melodyskyplus.Verify;
import net.mirolls.melodyskyplus.client.AntiBug;
import net.mirolls.melodyskyplus.modules.MelodyPlusModules;
import net.mirolls.melodyskyplus.modules.SmartyPathFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.module.Module;

@Mixin(value = ModuleManager.class, remap = false)
public class ModuleManagerMixin {
  @Shadow public static List<Module> modules;

  @Inject(method = "init", remap = false, at = @At("HEAD"))
  public void initHead(CallbackInfo ci) {
    List<Module> newModules = MelodyPlusModules.newModules();

    modules.addAll(newModules);
  }

  @Inject(method = "init", remap = false, at = @At("RETURN"))
  public void initReturn(CallbackInfo ci) {
    if (AntiBug.isBugRemoved() && Verify.isVerified()) {
      Objects.requireNonNull(SmartyPathFinder.getINSTANCE()).setEnabled(true);
    }
  }
}
