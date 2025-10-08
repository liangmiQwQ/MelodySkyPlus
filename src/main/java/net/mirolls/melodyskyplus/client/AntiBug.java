package net.mirolls.melodyskyplus.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class AntiBug {
  public static boolean removeBug(CallbackInfoReturnable<Boolean> cir) {
    return cir.getReturnValue();
  }

  public static boolean isBugRemoved() {
    return true;
  }
}