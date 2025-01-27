package net.mirolls.melodyskyplus.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class CheckPlayerFlying {
  private final List<Boolean> checkingData = new ArrayList<>();
  private EntityPlayer player = null;
  private boolean checking = false;
  private CheckCallBack callBack = null;

  public CheckPlayerFlying() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  public void setCallBack(CheckCallBack callBack) {
    this.callBack = callBack;
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (callBack != null && checking && player != null) {
      if (player.capabilities.isFlying) {
        callBack.callback(false);
        resetCheck();
      }

      // 在地面add true 否则add false
      boolean addingData = player.onGround || !player.isAirBorne;
      if (addingData) {
        callBack.callback(false);

      }
      
      checkingData.add(addingData);

      if (checkingData.size() >= 20 * 2 /*for 2s*/) {
        // 2秒多中下来 一直是false 说明 一直在飞

        callBack.callback(true);

        resetCheck();
      }
    }
  }


  public void resetCheck() {
    checking = false;
    callBack = null;
    player = null;
    checkingData.clear();
  }


  public EntityPlayer getPlayer() {
    return player;
  }

  public void setPlayer(EntityPlayer player) {
    this.player = player;
  }

  public boolean isChecking() {
    return checking;
  }

  public void setChecking(boolean checking) {
    this.checking = checking;
  }
}
