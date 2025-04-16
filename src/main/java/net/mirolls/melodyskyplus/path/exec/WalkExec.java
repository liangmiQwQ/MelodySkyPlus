package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.path.type.Node;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.Random;

import static net.mirolls.melodyskyplus.utils.PlayerUtils.smoothRotation;

public class WalkExec {
  public static void exec(Node nextNode, Minecraft mc, Node node) {
    // 转换到角度
    Vec3d center = Vec3d.ofCenter(nextNode.pos);
    Rotation rotation = RotationUtil.vec3ToRotation(center);

    // 移动玩家视角
    mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), new Random().nextFloat() / 5);
    mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 75F);

    // 控制行走
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

    double distanceToEnd = Math.hypot(mc.thePlayer.posX - center.getX(), mc.thePlayer.posZ - center.getZ());
    if (distanceToEnd > 6) {
      // 如果距离点距离较长 则进行一些额外处理
      if (Math.abs(mc.thePlayer.rotationYaw - rotation.getYaw()) < 0.5) {
        // 目前已经看着这个点了 设置疾跑
        mc.thePlayer.setSprinting(mc.thePlayer.getFoodStats().getFoodLevel() > 6 && mc.thePlayer.moveForward > 0.0F && !mc.thePlayer.isSneaking());

        // 利用node中记录的rotation与实际rotation的差值 通过a和d修正玩家位置
        float yawShould = node.nextRotation.getYaw();
        float yawNow = rotation.getYaw();


        float diff = PlayerUtils.getYawDiff(yawNow, yawShould);

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
    } else {
      // 取消疾跑
      mc.thePlayer.setSprinting(false);
    }
  }
}
