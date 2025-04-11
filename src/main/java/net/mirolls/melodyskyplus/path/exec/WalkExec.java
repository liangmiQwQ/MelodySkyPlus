package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.path.type.Node;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.Random;

import static net.mirolls.melodyskyplus.utils.PlayerUtils.smoothRotation;

public class WalkExec {
  public static void exec(Node nextNode, Minecraft mc, Node node) {
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


      float diff = yawNow - yawShould;
      if (diff > 180) {
        // 如果差value大于180的 可能是遇到了错误情况 (-90 ~ -180) 需要把这些坐标变成正确合理的坐标 (270 ~ 180)
        yawShould = (yawShould - (-180) + 180);
        diff = yawNow - yawShould;
      } else if (diff < -180) {
        yawNow = (yawNow - (-180) + 180);
        diff = yawNow - yawShould;
      }

      // 如果 now - should是正的 则偏左 则需要往右移动
      if (diff > 2) {
        MelodySkyPlus.LOGGER.info("Found Player offset to the left, start to go to the right. (" + yawNow + " - " + yawShould + ")");
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
      } else {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
      }

      // 反之亦然
      if (diff < -2) {
        MelodySkyPlus.LOGGER.info("Found Player offset to the right, start to go to the left. (" + yawNow + " - " + yawShould + ")");
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
      } else {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
      }
    } else {
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
    }
  }
}
