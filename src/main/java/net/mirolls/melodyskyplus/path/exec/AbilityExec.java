package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.mirolls.melodyskyplus.modules.SmartyPathFinder;
import net.mirolls.melodyskyplus.path.type.Ability;
import net.mirolls.melodyskyplus.path.type.Node;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.mirolls.melodyskyplus.utils.PlayerUtils.smoothRotation;

public class AbilityExec {
  private Stage stage = Stage.WALK_TO_ABILITY_START;
  private double lastMotion;
  private int goEndTicks = 0;

  public void exec(Node nextNode, List<Node> path, Minecraft mc, SmartyPathFinder smartyPathFinder, Node node) {
    // 同样 也是让后面代码更加轻松
    Ability nextAbility = (Ability) nextNode;
    Vec3d nextVec = Vec3d.ofCenter(nextNode.getPos());
    Node endNode = path.get(2);

    // 先处理一些关于条件的事情

    if (stage == Stage.WALK_TO_ABILITY_END) {
      // 此时可能才刚刚和方块搭边 我们要继续转头
      Rotation rotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(endNode.pos));

      mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), new Random().nextFloat() / 5);
      mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 75F);

      if (lastMotion * 0.8 < Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) && goEndTicks < 60) {
        // 每一步要走的至少比last motion大 否则有可能就碰壁了
        goEndTicks++;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
      } else {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

        stage = Stage.DECIDE_HOW_TO_WARP;
      }
    } else if (stage == Stage.WALK_TO_ABILITY_START_SLOWLY) {
      // 如果距离这个点比较近了 要避免冲出去
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);

      // 执行操作 先换物品到aotv
      mc.thePlayer.inventory.currentItem = smartyPathFinder.aotvSlot.getValue().intValue();

      // 记录lastMotion 有利于到点内后的判断
      lastMotion = Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);

      // 如果到位则切换到下一个阶段
      if (Objects.equals(nextAbility.getPos(), PlayerUtils.getPlayerLocation())) {
        stage = Stage.WALK_TO_ABILITY_END;
      }
    } else if (stage == Stage.WALK_TO_ABILITY_START) {
      // 如果还没走到这个节点 需要先走到
      PathExec.walkExec(nextNode, mc, node);

      // 切换到下一个阶段如果到位
      if (Math.hypot(mc.thePlayer.posX - nextVec.getX(), mc.thePlayer.posZ - nextVec.getZ()) < 2.5) {
        stage = Stage.WALK_TO_ABILITY_START_SLOWLY;
      }
    }
  }

  public enum Stage {
    WALK_TO_ABILITY_START,
    WALK_TO_ABILITY_END,
    WALK_TO_ABILITY_START_SLOWLY,
    DECIDE_HOW_TO_WARP,
  }
}
