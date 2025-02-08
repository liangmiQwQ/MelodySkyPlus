package net.mirolls.melodyskyplus.mixin;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mirolls.melodyskyplus.client.AntiBug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.System.Melody.Authentication.AuthManager;


@SideOnly(Side.CLIENT)
@Mixin(value = AuthManager.class, remap = false)
public class AuthManagerMixin {
  @Inject(method = "authMe", at = @At(value = "HEAD", remap = false), remap = false)
  private void authMe(CallbackInfoReturnable<Boolean> cir) {
    AntiBug.removeBug();
  }
}
