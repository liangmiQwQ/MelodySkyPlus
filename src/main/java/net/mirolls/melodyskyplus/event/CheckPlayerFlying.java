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
        callBack.callback(true);
        resetCheck();
      }

      // 在地面add true 否则add false
      boolean addingData = player.onGround || !player.isAirBorne;
      /* if (addingData) {
        callBack.callback(false);
      }*/

      checkingData.add(addingData);

      if (checkingData.size() >= 20 /*for 1s*/) {
        // 2s下来后开始检查数据
        int onGroundNumber = 0;
        for (Boolean checkedData : checkingData) {
          if (checkedData) {
            onGroundNumber++;
          }
        }

        callBack.callback(onGroundNumber <= 1);

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
