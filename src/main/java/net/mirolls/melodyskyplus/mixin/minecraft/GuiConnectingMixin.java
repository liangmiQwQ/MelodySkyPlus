package net.mirolls.melodyskyplus.mixin.minecraft;

import java.util.Objects;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.mirolls.melodyskyplus.client.AntiBug;
import net.mirolls.melodyskyplus.modules.AutoReconnect.AutoReconnect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiConnecting.class)
public class GuiConnectingMixin {
  @Inject(method = "connect", at = @At("HEAD"))
  private void connect(String host, int port, CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      Objects.requireNonNull(AutoReconnect.getInstance()).host = host + ":" + port;
    }
  }
}
