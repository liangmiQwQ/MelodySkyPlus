package net.mirolls.melodyskyplus.path.test;

import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CanGoRenderer {
  private List<BlockPos> path = null;

  public CanGoRenderer() {
    EventBus.getInstance().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }

  public void startRender(List<Vec3d> path) {
    List<BlockPos> blockPosList = new ArrayList<>();
    for (Vec3d vec : path) {
      blockPosList.add(new BlockPos(vec.toVec3()));
    }

    this.path = blockPosList;
  }

  public void clear() {
    this.path = null;
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    if (path != null && !path.isEmpty()) {
      for (BlockPos pathPos : path) {
        RenderUtil.drawFullBlockESP(pathPos, new Color(119, 71, 17, 100), event.getPartialTicks());
      }
    }
  }
}
