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

import static net.mirolls.melodyskyplus.utils.PlayerUtils.smoothRotation;

public class AbilityExec {
  public boolean rubbish = false;
  private Stage stage = Stage.WALK_TO_ABILITY_START;
  private int goEndTicks = 0;

  public void exec(Node nextNode, List<Node> path, Minecraft mc, SmartyPathFinder smartyPathFinder, Node node) {
    Ability nextAbility = (Ability) nextNode;
    Vec3d nextVec = Vec3d.ofCenter(nextNode.getPos());
    Node endNode = path.get(2);

    // 这次并非其他exec的条件主导 使用stage主导
    // 同时这里也需要加上一些基本条件
    Vec3d center = Vec3d.ofCenter(endNode.getPos());
    boolean isInBlock = Math.abs(mc.thePlayer.posX - center.getX()) <= 0.8 && Math.abs(mc.thePlayer.posZ - center.getZ()) <= 0.8 && mc.thePlayer.posY == endNode.getPos().getY();
    if (isInBlock) {
      // 如果已经到达了终点 则代表执行到现在一切都很好
      path.remove(0);
      // 不删除1的理由 见JumpExec部分
    }

    if (stage == Stage.WALK_TO_ABILITY_START) {
      if (Math.hypot(mc.thePlayer.posX - nextVec.getX(), mc.thePlayer.posZ - nextVec.getZ()) < 2.5) {
        // 切换到下一个阶段如果到位
        stage = Stage.WALK_TO_ABILITY_START_SLOWLY;
      } else {
        // 如果还没走到这个节点 需要先走到
        PathExec.walkExec(nextNode, mc, node);
      }
    } else if (stage == Stage.WALK_TO_ABILITY_START_SLOWLY) {
      // 如果距离这个点比较近了 要避免冲出去
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);

      if (Objects.equals(nextAbility.getPos(), PlayerUtils.getPlayerLocation())) {
        // 如果到位则切换到下一个阶段
        stage = Stage.WALK_TO_ABILITY_END;
      } else {
        // 执行操作 先换物品到aotv
        mc.thePlayer.inventory.currentItem = smartyPathFinder.aotvSlot.getValue().intValue();
      }
    } else if (stage == Stage.WALK_TO_ABILITY_END) {
      // 此时可能才刚刚和方块搭边 我们要继续转头
      Rotation rotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(endNode.pos));

      mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), new Random().nextFloat() / 5);
      mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 75F);

      goEndTicks++;

      if (goEndTicks < 50) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
      } else if (goEndTicks < 53) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
      } else {
        stage = Stage.DECIDE_HOW_TO_WARP;
      }
    } else if (stage == Stage.DECIDE_HOW_TO_WARP) {
      if (PlayerUtils.rayTrace(endNode.getPos()) && MathUtil.distanceToPos(endNode.getPos(), PlayerUtils.getPlayerLocation()) < 55) {
        // 情况1 可以进行etherWarp
        stage = Stage.ETHER_WARP;
      } else {
        // 情况2 先点一下后再进行etherWarp 不行就继续点
      }
    } else if (stage == Stage.ETHER_WARP) {
      // 转头到目标方块
      Rotation rotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(endNode.pos));

      mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), 60F);
      mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 60F);

      if (Math.abs(mc.thePlayer.rotationPitch - rotation.getPitch()) < 0.2 && Math.abs(mc.thePlayer.rotationYaw - rotation.getYaw()) < 0.2) {
        // 正在看着这个点
        Client.rightClick();
      }
    }
  }

  public enum Stage {
    WALK_TO_ABILITY_START,
    WALK_TO_ABILITY_END,
    WALK_TO_ABILITY_START_SLOWLY,
    DECIDE_HOW_TO_WARP,
    ETHER_WARP,
  }
}
