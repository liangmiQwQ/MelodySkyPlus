package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.mirolls.melodyskyplus.modules.SmartyPathFinder;
import net.mirolls.melodyskyplus.path.type.Node;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static net.mirolls.melodyskyplus.utils.PlayerUtils.smoothRotation;

public class MineExec {
  public boolean rubbish = false;
  public int tick = -1;
  Set<BlockPos> mineSet = new HashSet<>();


  public boolean exec(Node nextNode, Minecraft mc, List<Node> path, SmartyPathFinder smartyPathFinder, SkyblockArea area) {
    // 先切换到稿子
    mc.thePlayer.inventory.currentItem = smartyPathFinder.pickaxeSlot.getValue().intValue() - 1;

    // 先挖掘对应的方块
    BlockPos footBlock = nextNode.getPos();
    BlockPos headBlock = nextNode.getPos().up();


    Rotation footBlockRotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(footBlock));


    if (mc.theWorld.getBlockState(headBlock).getBlock() != Blocks.air) {
      return mine(mc, area, headBlock);
    } else if (mc.theWorld.getBlockState(footBlock).getBlock() != Blocks.air) {
      return mine(mc, area, footBlock);
    } else {
      // 停止挖掘
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);

      // 通路后 先转头
      mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, footBlockRotation.getPitch(), new Random().nextFloat() / 5);
      mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, footBlockRotation.getYaw(), 25F);

      // 转弯头开始走
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Math.abs(mc.thePlayer.rotationYaw - footBlockRotation.getYaw()) < 5);

      // 处理下一个点
      if (nextNode.getPos().equals(PlayerUtils.getPlayerLocation())) {
        rubbish = true;
        path.remove(0);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
      }
    }

    return true;
  }

  private boolean mine(Minecraft mc, SkyblockArea area, BlockPos block) {
    Vec3d blockCenter = Vec3d.ofCenter(block);
    if (Math.hypot(
        Math.hypot(
            mc.thePlayer.posX - blockCenter.getX(),
            mc.thePlayer.posZ - blockCenter.getZ()
        ),
        mc.thePlayer.posY + 1 - blockCenter.getY()
    ) > 5.0
    ) {
      return false;
    }


    Rotation footBlockRotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(block));

    // 停止走路 拒接转圈
    tick++;
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);

    // 转头到方块
    mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, footBlockRotation.getPitch(), 25);
    mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, footBlockRotation.getYaw(), 25);

    boolean canGo = RotationUtil.isLookingAtBlock(block)
        || (Math.abs(mc.thePlayer.rotationPitch - footBlockRotation.getPitch()) < 1
        && Math.abs(mc.thePlayer.rotationYaw - footBlockRotation.getYaw()) < 1);

    if (area.isIn(Areas.Crystal_Hollows)) {
      if (canGo) {
        if (!mineSet.contains(block)) {
          mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, block, EnumFacing.DOWN));
          mineSet.add(block);
        }
        mc.thePlayer.swingItem();
      }
    } else {
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), canGo);
    }

    return true;
  }
}
