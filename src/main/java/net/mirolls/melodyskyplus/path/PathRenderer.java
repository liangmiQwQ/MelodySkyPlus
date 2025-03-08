package net.mirolls.melodyskyplus.path;

import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Utils.render.RenderUtil;

import java.awt.*;
import java.util.List;

public class PathRenderer {
  private BlockPos target = null;
  private List<BlockPos> path = null;

  public PathRenderer() {
    EventBus.getInstance().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    if (target != null && path != null && !path.isEmpty()) {
      RenderUtil.drawFullBlockESP(target, new Color(44, 105, 133, 100), event.getPartialTicks());
      for (BlockPos pos : path) {
        RenderUtil.drawFullBlockESP(pos, new Color(8, 125, 13, 100), event.getPartialTicks());
      }
    }
  }

  public void startRender(BlockPos target, List<BlockPos> path) {
    this.target = target;
    this.path = path;
  }

  public void clear() {
    this.target = null;
    this.path = null;
  }
}
