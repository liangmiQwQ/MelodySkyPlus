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
  private List<PathPos> path = null;

  public PathRenderer() {
    EventBus.getInstance().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    if (target != null && path != null && !path.isEmpty()) {
      for (PathPos pathPos : path) {
        if (pathPos.isToMine()) {
          RenderUtil.drawFullBlockESP(pathPos.getPos(), new Color(8, 125, 13, 100), event.getPartialTicks());
        } else {
          RenderUtil.drawFullBlockESP(pathPos.getPos(), new Color(189, 2, 13, 100), event.getPartialTicks());
        }
      }

      RenderUtil.drawFullBlockESP(target, new Color(44, 105, 133, 100), event.getPartialTicks());
    }
  }

  public void startRender(BlockPos target, List<PathPos> path) {
    this.target = target;
    this.path = path;
  }

  public void clear() {
    this.target = null;
    this.path = null;
  }
}
