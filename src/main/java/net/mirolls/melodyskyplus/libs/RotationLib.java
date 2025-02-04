package net.mirolls.melodyskyplus.libs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xyz.Melody.Utils.math.Rotation;


public class RotationLib {

  Minecraft mc = Minecraft.getMinecraft();
  private Rotation targetRotation = null;
  private boolean rotating = false;
  private float speedCoefficient = 5f;
  private CallBack callBack;


  public RotationLib() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (rotating) {
      if (mc.thePlayer != null) {
        float oldYaw = this.mc.thePlayer.rotationYaw;
        float oldPitch = this.mc.thePlayer.rotationPitch;
        this.mc.thePlayer.rotationYaw = this.smoothRotation(this.mc.thePlayer.rotationYaw, targetRotation.getYaw(), 120.0F);
        this.mc.thePlayer.rotationPitch = this.smoothRotation(this.mc.thePlayer.rotationPitch, targetRotation.getPitch(), 120.0F);

        // 到位了就停止了
        if (Math.abs(oldYaw - this.mc.thePlayer.rotationYaw) < 1f && Math.abs(oldPitch - this.mc.thePlayer.rotationPitch) < 1f) {
          rotating = false;
          targetRotation = null;
          if (callBack != null) {
            callBack.callback();
          }
        }
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

    return current + deltaAngle / (2.0F * speedCoefficient);
  }

  public Rotation getTargetRotation() {
    return targetRotation;
  }

  public void setTargetRotation(Rotation targetRotation) {
    this.targetRotation = targetRotation;
  }

  public boolean isRotating() {
    return rotating;
  }

  public void startRotating() {
    rotating = true;
  }

  public float getSpeedCoefficient() {
    return speedCoefficient;
  }

  public void setSpeedCoefficient(float speedCoefficient) {
    this.speedCoefficient = speedCoefficient;
  }

  public CallBack getCallBack() {
    return callBack;
  }

  public void setCallBack(CallBack callBack) {
    this.callBack = callBack;
  }
}
