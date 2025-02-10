package net.mirolls.melodyskyplus.libs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.math.RotationUtil;

public class WalkLib {
  private boolean walking;
  private BlockPos targetBlockPos;
  private CallBack callBack;
  private boolean isForwardKeyPressed = false;


  public WalkLib() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    Minecraft mc = Minecraft.getMinecraft();
    if (walking && targetBlockPos != null && callBack != null) {
      if (RotationUtil.isLookingAtBlock(targetBlockPos)) {
        if (MathUtil.distanceToPos(targetBlockPos, mc.thePlayer.getPosition()) > 2) {
          if (!isForwardKeyPressed) {
            isForwardKeyPressed = true;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
          }
        } else {
          // 已经走到了
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
          isForwardKeyPressed = false;
          callBack.callback();

          walking = false;
          targetBlockPos = null;
          callBack = null;
        }
      } else {
        // 扭头 靠rotationLib
        MelodySkyPlus.rotationLib.setSpeedCoefficient(1.0F);
        MelodySkyPlus.rotationLib.setTargetRotation(RotationUtil.posToRotation(targetBlockPos));
        MelodySkyPlus.rotationLib.startRotating();
      }
    }
  }

  public void stop() {
    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
    isForwardKeyPressed = false;
    walking = false;
    targetBlockPos = null;
    callBack = null;
  }

  public boolean isWalking() {
    return walking;
  }

  public void startWalking() {
    this.walking = true;
  }

  public void setCallBack(CallBack callBack) {
    this.callBack = callBack;
  }

  public void setTargetBlockPos(BlockPos targetBlockPos) {
    this.targetBlockPos = targetBlockPos;
  }

}
