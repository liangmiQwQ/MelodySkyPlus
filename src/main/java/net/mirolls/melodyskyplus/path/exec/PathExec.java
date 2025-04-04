package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.path.type.Mine;
import net.mirolls.melodyskyplus.path.type.Node;
import net.mirolls.melodyskyplus.path.type.Walk;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.List;
import java.util.Random;

public class PathExec {

  public PathExec() {
    EventBus.getInstance().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void onTick(EventPreUpdate event) {
    Minecraft mc = Minecraft.getMinecraft();

    List<Node> path = MelodySkyPlus.smartyPathFinder.path;

    if (path != null && !path.isEmpty()) {
      if (path.size() == 1) {
        // 走到终点自动停止
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        MelodySkyPlus.smartyPathFinder.clear();
        return;
      }

      // 执行器核心 运行path
      Node node = path.get(0);
      Node nextNode = path.get(1);

      if (nextNode instanceof Walk) {
        // 转换到角度
        Rotation rotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(nextNode.pos));

        // 移动玩家视角
        mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), new Random().nextFloat() / 5);
        mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 75F);

        // 控制行走
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

        // 利用node中记录的rotation与实际rotation的差值 通过a和d修正玩家位置
        if (Math.abs(mc.thePlayer.rotationYaw - rotation.getYaw()) < 0.5) {
          // 开始纠正位置
          float yawShould = node.nextRotation.getYaw();
          float yawNow = rotation.getYaw();

          // 经过研究 发现如果正负号不统一的 并且角度小于-90度的 则需要将负号改为正号
          if (yawNow < -90 && yawShould > 90) {
            yawNow = -1 * yawNow;
          } else if (yawNow > 90 && yawShould < -90) {
            yawShould = -1 * yawShould;
          }

          // 如果 now - should是正的 则偏左 则需要往右移动
          if ((yawNow - yawShould) > 2) {
            MelodySkyPlus.LOGGER.info("Found Player offset to the left, start to go to the right.");
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
          } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
          }

          // 反之亦然
          if ((yawNow - yawShould) < -2) {
            MelodySkyPlus.LOGGER.info("Found Player offset to the right, start to go to the left.");
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
          } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
          }
        } else {
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        }

        // 如果玩家走到了点则停止
        Vec3d nextVec = Vec3d.ofCenter(nextNode.getPos());
        if (Math.hypot(mc.thePlayer.posX - nextVec.getX(), mc.thePlayer.posZ - nextVec.getZ()) < 1.5) {
          path.remove(0);
        }
      } else if (nextNode instanceof Mine) {
        // 先挖掘对应的方块
        BlockPos footBlock = nextNode.getPos();
        BlockPos headBlock = nextNode.getPos().up();

        Rotation footBlockRotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(footBlock));
        Rotation headBlockRotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(headBlock));


        if (mc.theWorld.getBlockState(headBlock).getBlock() != Blocks.air) {
          // 转头到方块
          mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, footBlockRotation.getPitch(), 75F);
          mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, footBlockRotation.getYaw(), 75F);

          // 清除头旁路障
          mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, headBlock, EnumFacing.DOWN));
          mc.thePlayer.swingItem();
        } else if (mc.theWorld.getBlockState(footBlock).getBlock() != Blocks.air) {
          // 转头到方块
          mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, headBlockRotation.getPitch(), 75F);
          mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, headBlockRotation.getYaw(), 75F);

          // 挖掘
          mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, footBlock, EnumFacing.DOWN));
          mc.thePlayer.swingItem();

          // ⬆️ 注意这里没有检查rayTrace是因为寻路寻找到的必定是可以直接看到的
        } else {
          // 通路后 先转头
          mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, footBlockRotation.getPitch(), new Random().nextFloat() / 5);
          mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, footBlockRotation.getYaw(), 75F);

          // 转弯头开始走
          if (Math.abs(mc.thePlayer.rotationYaw - footBlockRotation.getYaw()) < 5) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);

            Vec3d nextVec = Vec3d.ofCenter(nextNode.getPos());
            if (Math.abs(mc.thePlayer.posX - nextVec.getX()) < 1 && Math.abs(mc.thePlayer.posZ - nextVec.getZ()) < 1) {
              KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
              path.remove(0);
            }
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

    return (current + deltaAngle / 2) % 360;
  }

}
