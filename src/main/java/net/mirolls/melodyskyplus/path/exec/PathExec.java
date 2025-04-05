package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.path.type.Jump;
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
      }
    }
  }

  private void jumpExec(Node nextNode, List<Node> path, Minecraft mc, Node node) {
    // 提前先转化成Jump类型 为了事后做用方便
    Jump jumpNode = (Jump) nextNode;
    Node endNode = path.get(2);


    if (mc.thePlayer.onGround) {
      Vec3d jumpVec = Vec3d.ofCenter(jumpNode.getPos());
      if (Math.hypot(mc.thePlayer.posX - jumpVec.getX(), mc.thePlayer.posZ - jumpVec.getZ()) < jumpNode.jumpDistance) {
        // 到达跳跃范围
        mc.thePlayer.jump();
      } else {
        walkExec(jumpNode, mc, node);
      }
    } else {
      // 如果玩家没有到位的
      int x = (int) (mc.thePlayer.posX - mc.thePlayer.posX % 1 - 1);
      int z = (int) (mc.thePlayer.posZ - mc.thePlayer.posZ % 1 - 1);
      if (x != endNode.getPos().getX() || z != endNode.getPos().getZ()) {
        // 转换到角度
        Rotation rotation = RotationUtil.vec3ToRotation(Vec3d.ofCenter(nextNode.pos));

        // 移动玩家视角
        mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), new Random().nextFloat() / 5);
        mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 75F);

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
      } else {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        if (mc.thePlayer.onGround) {
          // 落地了 到达位置了 删除跳跃节点
          path.remove(1);
          path.remove(0);
        }
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

      // 经过研究 发现如果正负号不统一的 并且角度小于-90度的 则需要将负号改为正号
      if (yawNow < -90 && yawShould > 90) {
        yawNow = -1 * yawNow;
      } else if (yawNow > 90 && yawShould < -90) {
        yawShould = -1 * yawShould;
      }

      // 如果 now - should是正的 则偏左 则需要往右移动
      if ((yawNow - yawShould) > 2) {
        MelodySkyPlus.LOGGER.info("Found Player offset to the left, start to go to the right. (" + yawNow + " - " + yawShould + ")");
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
      } else {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
      }

      // 反之亦然
      if ((yawNow - yawShould) < -2) {
        MelodySkyPlus.LOGGER.info("Found Player offset to the left, start to go to the right. (" + yawNow + " - " + yawShould + ")");
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

      // 挖掘路障
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), RotationUtil.isLookingAtBlock(headBlock));
    } else if (mc.theWorld.getBlockState(footBlock).getBlock() != Blocks.air) {
      // 停止走路 拒接转圈
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);

      // 转头到方块
      mc.thePlayer.rotationPitch = smoothRotation(mc.thePlayer.rotationPitch, footBlockRotation.getPitch(), 25);
      mc.thePlayer.rotationYaw = smoothRotation(mc.thePlayer.rotationYaw, footBlockRotation.getYaw(), 25);

      KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), RotationUtil.isLookingAtBlock(footBlock));
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

}
