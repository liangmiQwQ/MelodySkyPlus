package net.mirolls.melodyskyplus.mixin;

import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.System.Commands.commands.AutoRubyCMD;
import xyz.Melody.Utils.Helper;

@Mixin(value = AutoRubyCMD.class, remap = false)
public class AutoRubyCMDMixin {
  @Inject(method = "execute", at = @At("HEAD"), remap = false)
  public void execute(String[] args, CallbackInfoReturnable<String> cir) {
    if (args.length >= 1) {
      if (args[0].toLowerCase().contains("adac")) {
        MelodySkyPlus.nukerTicks.reset();
        Helper.sendMessage("AutoRuby: Adaptive Mode Ticks Cleared.");
      }
    }
  }
}
