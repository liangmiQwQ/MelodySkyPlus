package net.mirolls.melodyskyplus.mixin;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.client.AntiBug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.System.Commands.commands.AutoRubyCMD;
import xyz.Melody.Utils.Helper;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;

@Mixin(value = AutoRubyCMD.class, remap = false)
public class AutoRubyCMDMixin {
  @Inject(method = "execute", at = @At("HEAD"), remap = false)
  public void execute(String[] args, CallbackInfoReturnable<String> cir) {
    if (args.length >= 1 && AntiBug.isBugRemoved()) {
      if (args[0].toLowerCase().contains("adac") || args[0].toLowerCase().contains("ac")) {
        MelodySkyPlus.nukerTicks.reset();
        Helper.sendMessage("AutoRuby: Adaptive Mode Ticks Cleared.");
      } else if (args[0].toLowerCase().contains("stop")) {
        // 重置此前设置的nukerTicks
        MelodySkyPlus.nukerTicks.reset();
        Helper.sendMessage("AutoRuby: Adaptive Mode Ticks Cleared.");
      } else if (args[0].toLowerCase().contains("start")) {
        // 重制卡jasper的状态
        MelodySkyPlus.jasperUsed.setJasperUsed(false);
      } else if (args[0].toLowerCase().contains("first")) {
        if (args.length == 4) {
          try {
            int xDiff = AutoRuby.getINSTANCE().wps.get(0).getX() - Integer.parseInt(args[1]);
            int yDiff = AutoRuby.getINSTANCE().wps.get(0).getY() - Integer.parseInt(args[2]);
            int zDiff = AutoRuby.getINSTANCE().wps.get(0).getZ() - Integer.parseInt(args[3]);

            AutoRuby.getINSTANCE().wps.replaceAll(blockPos -> {
              return blockPos.add(new BlockPos(-xDiff, -yDiff, -zDiff)); // 减去差值
            });
            Helper.sendMessage("Successfully to set first. ");
          } catch (NumberFormatException e) {
            Helper.sendMessage("You must send a number to set the first block. ");
          }
        }
      }
    }
  }
}
