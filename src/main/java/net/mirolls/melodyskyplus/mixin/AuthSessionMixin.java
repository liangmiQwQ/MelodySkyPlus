package net.mirolls.melodyskyplus.mixin;

import net.mirolls.melodyskyplus.client.AntiRat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.Melody.System.Melody.Authentication.AuthSession;

@Mixin(AuthSession.class)
public class AuthSessionMixin {
  @Inject(method = "getSessionID", at = {@At("RETURN")}, remap = false, cancellable = true)
  private static void getSessionID(CallbackInfoReturnable<String> cir) {
    cir.setReturnValue(AntiRat.antiRats(cir));
  }
}
