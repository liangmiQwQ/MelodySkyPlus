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
  public boolean rubbish = false;
  private Stage stage = Stage.WALK_TO_ABILITY_START;
  private int goEndTicks = 0;

  public void exec(Node nextNode, List<Node> path, Minecraft mc, SmartyPathFinder smartyPathFinder, Node node) {
    Ability nextAbility = (Ability) nextNode;
    Vec3d nextVec = Vec3d.ofCenter(nextNode.getPos());
    Node endNode = path.get(2);

    // 主体部分
    // 这次并非其他exec的条件主导 使用stage主导
    if (stage == Stage.WALK_TO_ABILITY_END) {
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
      // New Thing
    } else if (stage == Stage.WALK_TO_ABILITY_START_SLOWLY) {
      // 如果距离这个点比较近了 要避免冲出去
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);

      // 执行操作 先换物品到aotv
      mc.thePlayer.inventory.currentItem = smartyPathFinder.aotvSlot.getValue().intValue();

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
