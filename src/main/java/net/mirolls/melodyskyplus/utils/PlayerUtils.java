package net.mirolls.melodyskyplus.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.RotationUtil;

public class PlayerUtils {
  public static float smoothRotation(float current, float target, float maxIncrement) {
    float deltaAngle = MathHelper.wrapAngleTo180_float(target - current);
    if (deltaAngle > maxIncrement) {
      deltaAngle = maxIncrement;
    }

    if (deltaAngle < -maxIncrement) {
      deltaAngle = -maxIncrement;
    }

    return MathHelper.wrapAngleTo180_float((current + deltaAngle / 2) % 360);
  }

  public static boolean rayTrace(BlockPos blockPos) {
    Vec3d target = null;
    Vec3d[] var2 = Vec3d.points(blockPos);
    int var3 = var2.length;
    int var4 = 0;

    while (var4 < var3) {
      Vec3d vec = var2[var4];
      MovingObjectPosition trajectory = RotationUtil.rayTrace(vec);
      if (trajectory == null) {
        target = vec;
        break;
      }

      label69:
      {
        if (trajectory.entityHit == null || trajectory.entityHit == Minecraft.getMinecraft().thePlayer) {
          if (trajectory.getBlockPos() == null) {
            break label69;
          }

          boolean sameX = trajectory.getBlockPos().getX() == blockPos.getX();
          boolean sameY = trajectory.getBlockPos().getY() == blockPos.getY();
          boolean sameZ = trajectory.getBlockPos().getZ() == blockPos.getZ();
          if (sameX && sameY && sameZ) {
            break label69;
          }
        }

        ++var4;
        continue;
      }

      target = vec;
      break;
    }

    return target != null;
  }

  public static BlockPos getPlayerLocation() {
    Minecraft mc = Minecraft.getMinecraft();
    return getPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
  }

  public static BlockPos getPosition(double posX, double posY, double posZ) {
    int x = (int) Math.floor(posX);
    int y = (int) Math.floor(posY);
    int z = (int) Math.floor(posZ);
    return new BlockPos(x, y, z); // Minecraft提供的.getPosition不好用 返回的位置经常有较大的误差 这样是最保险的
  }
}
