package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.client.AntiBug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;

@Mixin(value = xyz.Melody.System.Melody.Account.GuiAltManager.class, remap = false)
public class GuiAltManager {

  @ModifyArg(method = "func_73863_a",
      at = @At(value = "INVOKE", ordinal = 1, target = "Lxyz/Melody/GUI/Font/CFontRenderer;drawString(Ljava/lang/String;FFI)F", remap = false),
      remap = false, index = 0)
  public String func_73863_a(String text) {
    if (text.startsWith("UUID: ")) {
      if (Objects.equals(MelodySkyPlus.antiBug.getBugID(), Minecraft.getMinecraft().getSession().getProfile().getId().toString())) {
        return "Melody+ Verified: " + (AntiBug.isBugRemoved() ? EnumChatFormatting.GREEN + "true" : EnumChatFormatting.GRAY + "false");
      } else {
        return "Melody+暂不支持切换账号";
      }
    } else {
      return text;
    }
  }
}
