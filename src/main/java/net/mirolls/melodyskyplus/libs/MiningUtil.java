package net.mirolls.melodyskyplus.libs;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.math.RotationUtil;

public class MiningUtil {
  private CheckCallBack callBack;
  private boolean mining;
  private BlockPos miningBP;

  public MiningUtil() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  public void setCallBack(CheckCallBack callBack) {
    this.callBack = callBack;
  }

  public void setMining(boolean mining) {
    this.mining = mining;
  }

  public void setMiningBP(BlockPos miningBP) {
    this.miningBP = miningBP;
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    Minecraft mc = Minecraft.getMinecraft();
    if (mining && miningBP != null && callBack != null) {
      if (Objects.equals(
          mc.theWorld.getBlockState(miningBP).getBlock().getRegistryName(),
          Blocks.air.getRegistryName())) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        callBack.callback(true);
        mining = false;
        miningBP = null;
        callBack = null;
      } else {
        if (RotationUtil.isLookingAtBlock(miningBP)
            && MathUtil.distanceToPos(mc.thePlayer.getPosition(), miningBP) < 3) {
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
          //          mc.thePlayer.sendQueue.addToSendQueue(new
          // C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
          // this.miningBP, EnumFacing.DOWN));

          KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
          //          mc.thePlayer.swingItem();
        }
      }
    }
  }
}
