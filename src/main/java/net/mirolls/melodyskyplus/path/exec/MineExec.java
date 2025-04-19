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

import java.util.List;
import java.util.Random;

import static net.mirolls.melodyskyplus.utils.PlayerUtils.smoothRotation;

public class MineExec {
  public static void exec(Node nextNode, Minecraft mc, List<Node> path, SmartyPathFinder smartyPathFinder, SkyblockArea area) {
    // 先切换到稿子
    mc.thePlayer.inventory.currentItem = smartyPathFinder.pickaxeSlot.getValue().intValue() - 1;

    // 先挖掘对应的方块
    BlockPos footBlock = nextNode.getPos();
    BlockPos headBlock = nextNode.getPos().up();


    Rotation footBlockRotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(footBlock));
    Rotation headBlockRotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(headBlock));


    if (mc.theWorld.getBlockState(headBlock).getBlock() != Blocks.air) {
      // 停止走路 拒接转圈
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);

      // 转头到方块
      mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, headBlockRotation.getPitch(), 25);
      mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, headBlockRotation.getYaw(), 25);

      boolean canGo = RotationUtil.isLookingAtBlock(headBlock)
          || (Math.abs(mc.thePlayer.rotationPitch - headBlockRotation.getPitch()) < 1
          && Math.abs(mc.thePlayer.rotationYaw - headBlockRotation.getYaw()) < 1);

      if (area.isIn(Areas.Crystal_Hollows)) {
        if (canGo) {
          mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, headBlock, EnumFacing.DOWN));
          mc.thePlayer.swingItem();
        }
      } else {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), canGo);
      }
    } else if (mc.theWorld.getBlockState(footBlock).getBlock() != Blocks.air) {
      // 停止走路 拒接转圈
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);

      // 转头到方块
      mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, footBlockRotation.getPitch(), 25);
      mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, footBlockRotation.getYaw(), 25);

      boolean canGo = RotationUtil.isLookingAtBlock(footBlock)
          || (Math.abs(mc.thePlayer.rotationPitch - footBlockRotation.getPitch()) < 1
          && Math.abs(mc.thePlayer.rotationYaw - footBlockRotation.getYaw()) < 1);

      if (area.isIn(Areas.Crystal_Hollows)) {
        if (canGo) {
          mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, footBlock, EnumFacing.DOWN));
          mc.thePlayer.swingItem();
        }
      } else {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), canGo);
      }
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
        path.remove(0);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
      }
    }
  }
}
