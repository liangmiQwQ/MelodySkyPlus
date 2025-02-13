package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.System.Commands.commands.AutoRubyCMD;
import xyz.Melody.Utils.Helper;
import xyz.Melody.module.FMLModules.PlayerSoundHandler;

@Mixin(value = AutoRubyCMD.class, remap = false)
public class AutoRubyCMDMixin {
  @Inject(method = "execute", at = @At("HEAD"), remap = false)
  public void execute(String[] args, CallbackInfoReturnable<String> cir) {
    if (args.length >= 1) {
      if (args[0].toLowerCase().contains("adac")) {
        MelodySkyPlus.nukerTicks.reset();
        Helper.sendMessage("AutoRuby: Adaptive Mode Ticks Cleared.");
      } else if (args[0].toLowerCase().contains("stop")) {
        MelodySkyPlus.nukerTicks.reset();
        Helper.sendMessage("AutoRuby: Adaptive Mode Ticks Cleared.");
      } else if (args[0].toLowerCase().contains("test")) {
        try {
          PlayerSoundHandler.addSound("mob.ghast.charge", 5.0F, 1.5F, 5);
          PlayerSoundHandler.addSound("mob.ghast.death", 5.0F, 1.5F, 5);
          PlayerSoundHandler.addSound("mob.ghast.scream", 5.0F, 1.5F, 5);

          Minecraft mc = Minecraft.getMinecraft();
          mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("mob.ghast.charge")));
          mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("mob.ghast.death")));
          mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("mob.ghast.scream")));
        } catch (RuntimeException e) {
          MelodySkyPlus.LOGGER.error("Cannot play sounds while macro check!");
        }

      }
    }
  }
}
