package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.path.type.*;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.Utils.Helper;
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
        walkExec(nextNode, mc, node);

        // 这里给停止部分单独拉出来 是为了让Jump的代码复用
        Vec3d nextVec = Vec3d.ofCenter(nextNode.getPos());
        if (Math.hypot(mc.thePlayer.posX - nextVec.getX(), mc.thePlayer.posZ - nextVec.getZ()) < 1.5) {
          path.remove(0);
        }
      } else if (nextNode instanceof Mine) {
        mineExec(nextNode, mc, path);
      } else if (nextNode instanceof Jump) {
        jumpExec(nextNode, path, mc, node);
      } else if (nextNode instanceof Ability) {

      }
    }
  }

  private void jumpExec(Node nextNode, List<Node> path, Minecraft mc, Node node) {
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
        path.remove(1);
        path.remove(0);
      } else {
        // 情况2就是还没有起跳 先准备起跳
        Vec3d jumpVec = Vec3d.ofCenter(jumpNode.getPos());
        if (Math.hypot(mc.thePlayer.posX - jumpVec.getX(), mc.thePlayer.posZ - jumpVec.getZ()) < jumpNode.jumpDistance) {
          // 到达跳跃范围
          mc.thePlayer.jump();
        } else {
          walkExec(jumpNode, mc, node);
        }
      }
    } else {
      // 如果玩家没有到位的
      if (isInBlock) {
        // BUG 由于MOJANG设置的无脑惯性 需要按S抵消一下惯性(maybe)
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);

        // 这里什么时候松 要不要按 依然有问题
        boolean isInBlockNext = Math.abs(mc.thePlayer.posX + mc.thePlayer.motionX * tickToGround(endNode.getPos().getY()) - center.getX()) <= 0.8 && Math.abs(mc.thePlayer.posZ + mc.thePlayer.motionZ * tickToGround(endNode.getPos().getY()) - center.getZ()) <= 0.8;
        if (!isInBlockNext) {
          Helper.sendMessage("mx: " + mc.thePlayer.motionX + "; mz:" + mc.thePlayer.motionZ);
          Helper.sendMessage("rx: " + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) + "; rz:" + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ));
        }
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

  private void walkExec(Node nextNode, Minecraft mc, Node node) {
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

  private void mineExec(Node nextNode, Minecraft mc, List<Node> path) {
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
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), canGo);
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
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), canGo);
    } else {
      // 停止挖掘
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);

      // 通路后 先转头
      mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, footBlockRotation.getPitch(), new Random().nextFloat() / 5);
      mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, footBlockRotation.getYaw(), 25F);

      // 转弯头开始走
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Math.abs(mc.thePlayer.rotationYaw - footBlockRotation.getYaw()) < 5);

      // 处理下一个点
      int x = (int) (mc.thePlayer.posX - mc.thePlayer.posX % 1 - 1);
      int y = (int) (mc.thePlayer.posY - mc.thePlayer.posY % 1);
      int z = (int) (mc.thePlayer.posZ - mc.thePlayer.posZ % 1 - 1);
      BlockPos posPlayer = new BlockPos(x, y, z); // Minecraft提供的.getPosition不好用 返回的位置经常有较大的误差 这样是最保险的
      if (nextNode.getPos().equals(posPlayer)) {
        path.remove(0);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
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

    return MathHelper.wrapAngleTo180_float((current + deltaAngle / 2) % 360);
  }

  private int tickToGround(int y) {
    Minecraft mc = Minecraft.getMinecraft();
    // 通过精准的计算 获得从最高点下降到现在的tick
    // 通过公式 3.92 * (1 - Math.pow(0.98, fallTick)) 精准求出顺时下落速度

    if (mc.thePlayer.motionY >= 3.91) {
      // 速度最大值 直接返回除数
      return (int) Math.round(mc.thePlayer.posY - y / 3.92);
    } else {
      int tickNow = (int) Math.round(Math.log(1 - mc.thePlayer.motionY / 3.92) / Math.log(0.98));
      // 这是现在的tick 根据玩家的y坐标 逐渐减去测算出来的速度 直到小于y后返回
      double playerY = mc.thePlayer.posY;
      int tickNeed = 0;
      
      while (playerY < y) {
        tickNeed++;
        playerY -= 3.92 * (1 - Math.pow(0.98, tickNeed + tickNow));
      }

      return tickNeed;
    }
  }
}
