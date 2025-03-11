package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.multiplayer.GuiConnecting;
import net.mirolls.melodyskyplus.modules.AutoReconnect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(GuiConnecting.class)
public class GuiConnectingMixin {
  @Inject(method = "connect", at = @At("HEAD"))
  private void connect(String host, int port, CallbackInfo ci) {
    Objects.requireNonNull(AutoReconnect.getInstance()).host = host + ":" + port;
  }
}
