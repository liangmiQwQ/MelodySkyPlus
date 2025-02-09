package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.module.modules.macros.Mining.PinglessMining;

@Mixin(value = PinglessMining.class, remap = false)
public class PinglessMiningMixin {
  @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
  public void tick(EventTick event, CallbackInfo ci) {
    if (!Minecraft.getMinecraft().thePlayer.onGround) ci.cancel();
  }

  @Inject(method = "onRender", at = @At("HEAD"), cancellable = true, remap = false)
  public void onRender(EventRender3D event, CallbackInfo ci) {
    if (!Minecraft.getMinecraft().thePlayer.onGround) ci.cancel();
  }
}
