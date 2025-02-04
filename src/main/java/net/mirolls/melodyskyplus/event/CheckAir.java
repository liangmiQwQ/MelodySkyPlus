package net.mirolls.melodyskyplus.event;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Objects;

public class CheckAir {
  private CheckCallBack callBack;
  private boolean checking;
  private BlockPos checkBP;

  public CheckAir() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  public void setCallBack(CheckCallBack callBack) {
    this.callBack = callBack;
  }

  public void setChecking(boolean checking) {
    this.checking = checking;
  }

  public void setCheckBP(BlockPos checkBP) {
    this.checkBP = checkBP;
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (checking && checkBP != null && callBack != null) {
      if (Objects.equals(Minecraft.getMinecraft().theWorld.getBlockState(checkBP).getBlock().getRegistryName(), Blocks.air.getRegistryName())) {
        callBack.callback(true);
        checking = false;
        checkBP = null;
        callBack = null;
      }
    }
  }
}
