package net.mirolls.melodyskyplus.mixin;

import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = xyz.Melody.System.Melody.Account.GuiAltManager.class, remap = false)
public class GuiAltManager {

  @ModifyArg(method = "func_73863_a",
      at = @At(value = "INVOKE", remap = false, target = "Lxyz/Melody/GUI/Font/CFontRenderer;drawString(Ljava/lang/String;FFI)I"),
      remap = false, index = 0)
  public String func_73863_a(String text) {
    return MelodySkyPlus.verify(text);
  }
}
