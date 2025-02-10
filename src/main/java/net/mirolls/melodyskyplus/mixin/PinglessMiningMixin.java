package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Value;
import xyz.Melody.module.Module;
import xyz.Melody.module.modules.macros.Mining.PinglessMining;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mixin(value = PinglessMining.class, remap = false)
public class PinglessMiningMixin {
  private Numbers<Double> melodySkyPlus$bps = new Numbers<>("BlocksPerSecond", 20.0, 1.0, 100.0, 1.0);

  @Inject(method = "<init>", at = @At("RETURN"), remap = false)
  public void init(CallbackInfo ci) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = Module.class.getDeclaredMethod("addValues", Value[].class);
    method.setAccessible(true);
    method.invoke(this, (Object) new Value[]{melodySkyPlus$bps});
  }

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
  public void tick(EventTick event, CallbackInfo ci) {
    if (!Minecraft.getMinecraft().thePlayer.onGround) ci.cancel();
  }

  @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lxyz/Melody/Utils/timer/TimerUtil;hasReached(D)Z", remap = false), remap = false)
  public double tick(double milliseconds) {
    return 1000 / melodySkyPlus$bps.getValue();
  }


  @Inject(method = "onRender", at = @At("HEAD"), cancellable = true, remap = false)
  public void onRender(EventRender3D event, CallbackInfo ci) {
    if (!Minecraft.getMinecraft().thePlayer.onGround) ci.cancel();
  }
}
