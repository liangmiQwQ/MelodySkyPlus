package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.path.type.Jump;
import net.mirolls.melodyskyplus.path.type.Node;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.List;
import java.util.Random;

import static net.mirolls.melodyskyplus.utils.PlayerUtils.smoothRotation;

public class JumpExec {
  public static void exec(Node nextNode, List<Node> path, Minecraft mc, Node node) {
    // 提前先转化成Jump类型 为了事后做用方便
    Jump jumpNode = (Jump) nextNode;
    Node endNode = path.get(2);


    Vec3d center = Vec3d.ofCenter(endNode.getPos());
    boolean isInBlock = Math.abs(mc.thePlayer.posX - center.getX()) <= 0.8 && Math.abs(mc.thePlayer.posZ - center.getZ()) <= 0.8;


    if (mc.thePlayer.onGround) {
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
      // 如果在陆地上 则有2个情况
      if (isInBlock) {
        // 落地了 到达位置了 删除跳跃节点
        // path.remove(1); 经过研究 发现不能一次性删除2个 否则如果有连续跳跃 将会出现极大的问题 这个节点应该由未来的该节点来亲自删除
        path.remove(0);
      } else {
        // 情况2就是还没有起跳 先准备起跳
        Vec3d jumpVec = Vec3d.ofCenter(jumpNode.getPos());
        if (Math.hypot(mc.thePlayer.posX - jumpVec.getX(), mc.thePlayer.posZ - jumpVec.getZ()) < jumpNode.jumpDistance) {
          // 到达跳跃范围
          // 先停止按下w然后再跳
          if (mc.gameSettings.keyBindForward.isKeyDown()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
          } else {
            mc.thePlayer.jump();
          }
        } else {
          WalkExec.exec(jumpNode, mc, node);
        }
      }
    } else {
      // 如果玩家没有到位的
      if (isInBlock) {
        // BUG 由于MOJANG设置的无脑惯性 需要按S抵消一下惯性(maybe)
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);

        int tick = tickToGround(endNode.getPos().getY());
        MelodySkyPlus.LOGGER.info(tick + " " + mc.thePlayer.motionX + " " + mc.thePlayer.motionZ); // 最保险的方法 除了略小误差 保证能精准计算到落地时间
        boolean isInBlockNext = Math.abs(mc.thePlayer.posX + mc.thePlayer.motionX * tick - center.getX()) <= 0.8 && Math.abs(mc.thePlayer.posZ + mc.thePlayer.motionZ * tick - center.getZ()) <= 0.8;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), !isInBlockNext);
      } else {
        // 转换到角度
        Rotation rotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(endNode.getPos()));

        // 移动玩家视角
        mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), new Random().nextFloat() / 5);
        mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 75F);

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
      }
    }
  }

  private static int tickToGround(int y) {
    Minecraft mc = Minecraft.getMinecraft();
    // 通过精准的计算 获得从最高点下降到现在的tick
    // 通过公式 3.92 * (1 - Math.pow(0.98, fallTick)) 精准求出顺时下落速度

    double absMotionY = Math.abs(mc.thePlayer.motionY);
    if (absMotionY >= 3.91) {
      // 速度最大值 直接返回除数
      return (int) Math.round((mc.thePlayer.posY - y) / 3.92);
    } else {
      int tickNow = (int) Math.round(Math.log(1 - absMotionY / 3.92) / Math.log(0.98));
      // 这是现在的tick 根据玩家的y坐标 逐渐减去测算出来的速度 直到小于y后返回
      double playerY = mc.thePlayer.posY;
      int tickNeed = 0;

      while (playerY > y) {
        tickNeed++;
        playerY -= 3.92 * (1 - Math.pow(0.98, tickNeed + tickNow));
      }

      return tickNeed;
    }
  }
}
