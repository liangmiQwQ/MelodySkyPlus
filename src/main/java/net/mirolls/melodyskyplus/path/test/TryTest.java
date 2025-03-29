package net.mirolls.melodyskyplus.path.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.render.RenderUtil;

import java.util.ArrayList;

public class TryTest {
  public int tick;
  public ArrayList<Vec3d> pathsPlayer = new ArrayList<>();
  private double angle;


  public TryTest() {
    EventBus.getInstance().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }

  public static double calcAngle(double l, double r) {
    // 计算每次应该旋转的角度
    if (r == 0) {
      throw new ArithmeticException("R cannot be zero");
    }
    return (Math.PI - 2 * Math.atan(l / (2 * r))) * (180 / Math.PI);
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    RenderUtil.drawLines(pathsPlayer, 1.0F, event.getPartialTicks());
  }

  @EventHandler
  public void onTick(EventTick event) {
    Minecraft mc = Minecraft.getMinecraft();
    if (tick > -1) {
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
      pathsPlayer.add(new Vec3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
      tick++;

      if (tick == 1) {
        mc.thePlayer.rotationYaw = 180;
      } else if (tick == 20) {
        double tickMove = Math.hypot(mc.thePlayer.posX - mc.thePlayer.lastTickPosX, mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);
        Helper.sendMessage(tickMove);
        angle = calcAngle(tickMove, 10);
      }

      if (tick > 60 && mc.thePlayer.rotationYaw > 90) {
//        mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, 90F, 180 - (float) angle);
        mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, 90F, 360);
      }

      if (tick == 320) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        Helper.sendMessage(angle);

        tick = -1;
      }

    }
  }

  private float smoothRotation(float current, float target, float maxIncrement) {
    float deltaAngle = MathHelper.wrapAngleTo180_float(target - current);
    if (deltaAngle > maxIncrement) {
      deltaAngle = maxIncrement;
    }

    if (deltaAngle < -maxIncrement) {
      deltaAngle = -maxIncrement;
    }

    return (current + deltaAngle / 2.0F) % 360;
  }
}
