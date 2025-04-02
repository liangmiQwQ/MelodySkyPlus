package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.path.type.Node;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;

import java.util.List;

public class PathExec {

  public PathExec() {
    EventBus.getInstance().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void onTick(EventTick event) {
    Minecraft mc = Minecraft.getMinecraft();

    List<Node> path = MelodySkyPlus.smartyPathFinder.path;

    if (path != null && !path.isEmpty()) {
      if (path.size() == 1) {
        MelodySkyPlus.smartyPathFinder.clear();
        return;
      }

      // 执行器核心 运行path
      Node node = path.get(0);
      Node nextNode = path.get(1);

      // 转换到角度
      Rotation rotation = RotationUtil.posToRotation(nextNode.pos);

      // 移动玩家视角
      mc.thePlayer.rotationPitch = RotationUtil.smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), 0.3f);
      mc.thePlayer.rotationYaw = RotationUtil.smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 120);

      // 控制行走
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);

      // TODO 利用node中记录的rotation与实际rotation的差值 通过a和d修正玩家位置

      // 如果玩家走到了点则停止
      Vec3d nextVec = Vec3d.ofCenter(nextNode.getPos());
      if (Math.hypot(mc.thePlayer.posX - nextVec.getX(), mc.thePlayer.posZ - nextVec.getZ()) < 1.5) {
        path.remove(0);
      }
    }
  }

}
