package net.mirolls.melodyskyplus.mixin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.mirolls.melodyskyplus.client.AntiBug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.Value;
import xyz.Melody.module.Module;
import xyz.Melody.module.modules.macros.Mining.PinglessMining;

@Mixin(value = PinglessMining.class, remap = false)
public class PinglessMiningMixin {
  private Numbers<Double> melodySkyPlus$bps =
      new Numbers<>("BlocksPerSecond", 20.0, 1.0, 100.0, 1.0);
  private Option<Boolean> melodySkyPlus$disableInAir = new Option<>("Disable While Jumping", true);

  @Inject(method = "<init>", at = @At("RETURN"), remap = false)
  public void init(CallbackInfo ci)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (AntiBug.isBugRemoved()) {
      Method method = Module.class.getDeclaredMethod("addValues", Value[].class);
      method.setAccessible(true);
      method.invoke(this, (Object) new Value[] {melodySkyPlus$bps, melodySkyPlus$disableInAir});
    }
  }

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
  public void tick(EventTick event, CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      if (melodySkyPlus$disableInAir.getValue()) {
        if (!Minecraft.getMinecraft().thePlayer.onGround) ci.cancel();
      }
    }
  }

  @ModifyArg(
      method = "tick",
      at =
          @At(
              value = "INVOKE",
              target = "Lxyz/Melody/Utils/timer/TimerUtil;hasReached(D)Z",
              remap = false),
      remap = false)
  public double tick(double milliseconds) {
    if (AntiBug.isBugRemoved()) {
      return 1000 / melodySkyPlus$bps.getValue();
    }
    return 50.0F;
  }

  @Inject(method = "onRender", at = @At("HEAD"), cancellable = true, remap = false)
  public void onRender(EventRender3D event, CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      if (!Minecraft.getMinecraft().thePlayer.onGround) ci.cancel();
    }
  }
}
