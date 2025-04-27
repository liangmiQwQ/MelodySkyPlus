package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.mirolls.melodyskyplus.modules.SmartyPathFinder;
import net.mirolls.melodyskyplus.path.type.Ability;
import net.mirolls.melodyskyplus.path.type.Node;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.Client;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AbilityExec {
  public boolean rubbish = false;
  public int tick = -1;
  private int lastRightClick = 10;


  public void exec(List<Node> path, Minecraft mc, Node node) {
    Ability nextAbility = (Ability) path.get(1);
    Vec3d nextVec = Vec3d.ofCenter(nextAbility.pos);
    Node endNode = path.get(2);
    Vec3d centerEnd = Vec3d.ofCenter(endNode.pos);

    // 切物品到aotv
    mc.thePlayer.inventory.currentItem = Objects.requireNonNull(SmartyPathFinder.getINSTANCE()).aotvSlot.getValue().intValue() - 1;

    // 终止 结束条件
    boolean isInBlock = Math.abs(mc.thePlayer.posX - centerEnd.getX()) <= 0.8 && Math.abs(mc.thePlayer.posZ - centerEnd.getZ()) <= 0.8 && Math.abs(mc.thePlayer.posY - centerEnd.getY()) <= 1;
    if (isInBlock) {
      // 如果已经到达了终点 则代表执行到现在一切都很好
      path.remove(0);
      rubbish = true;
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
      // 不删除1的理由 见JumpExec部分
    }
    boolean addTick = true;

    double targetX = nextAbility.pos.getX();
    double targetZ = nextAbility.pos.getZ();
    // 检查 是正在warp还是正在走路
    int xDiff = endNode.getPos().getX() - nextAbility.getPos().getX();
    if (xDiff != 0) {
      int var1 = xDiff / Math.abs(xDiff);
      // 存储正负号信息
      targetX = nextVec.getX() + var1 * 0.8;
    }

    int zDiff = endNode.getPos().getZ() - nextAbility.getPos().getZ();
    if (zDiff != 0) {
      int var1 = zDiff / Math.abs(zDiff);
      // 存储正负号信息
      targetZ = nextVec.getZ() + var1 * 0.8;
    }

    boolean warping = Math.abs(mc.thePlayer.posX - targetX) < 0.05 || Math.abs(mc.thePlayer.posZ - targetZ) < 0.05 || tick > 180;

    if (warping) {
      if (PlayerUtils.rayTrace(endNode.getPos().down()) && MathUtil.distanceToPos(endNode.getPos().down(), PlayerUtils.getPlayerLocation()) < 55) {
        // 可以进行etherWarp
        // 保持下蹲
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);

        // 转头到目标方块
        Rotation rotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(endNode.pos.down()));

        mc.thePlayer.rotationPitch = PlayerUtils.smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), 60F);
        mc.thePlayer.rotationYaw = PlayerUtils.smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 60F);

        if (Math.abs(mc.thePlayer.rotationPitch - rotation.getPitch()) < 0.4 && Math.abs(mc.thePlayer.rotationYaw - rotation.getYaw()) < 0.4) {
          // 正在看着这个点
          if (lastRightClick > 30) {
            Client.rightClick();
            lastRightClick = 0;
          }
          lastRightClick++;
        }

      } else {
        // 尝试retry
        tick = 4000;
      }
    } else {
      if (Math.hypot(mc.thePlayer.posX - nextVec.getX(), mc.thePlayer.posZ - nextVec.getZ()) < 4) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);

        // 依然需要移动玩家视角来避免错误位置
        Rotation rotation = RotationUtil.vec3ToRotation(centerEnd);

        // 移动玩家视角
        mc.thePlayer.rotationPitch = PlayerUtils.smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), new Random().nextFloat() / 5);
        mc.thePlayer.rotationYaw = PlayerUtils.smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 75F);
      } else {
        // 没走到 先走到
        addTick = false;
        WalkExec.exec(nextAbility, mc, node);
      }
    }

    if (addTick) tick++;
  }
}
