package net.mirolls.melodyskyplus.path;

import net.minecraftforge.common.MinecraftForge;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PathRenderer {
  private List<PathPos> path = null;
  private List<PathPos> shortPath = null;

  public PathRenderer() {
    EventBus.getInstance().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    if (path != null && !path.isEmpty() && shortPath != null && !shortPath.isEmpty()) {
      RenderUtil.drawLines((ArrayList<Vec3d>) PathPos.toVec3dArray(shortPath), 1.0F, event.getPartialTicks());
      for (PathPos pathPos : path) {
        if (pathPos.getType() == PathNodeType.WALK) {
          RenderUtil.drawFullBlockESP(pathPos.getPos(), new Color(8, 125, 13, 100), event.getPartialTicks());
        } else if (pathPos.getType() == PathNodeType.MINE) {
          RenderUtil.drawFullBlockESP(pathPos.getPos(), new Color(189, 2, 13, 100), event.getPartialTicks());
        } else if (pathPos.getType() == PathNodeType.ABILITY_START) {
          RenderUtil.drawFullBlockESP(pathPos.getPos(), new Color(37, 200, 255, 100), event.getPartialTicks());
        } else if (pathPos.getType() == PathNodeType.ABILITY_END) {
          RenderUtil.drawFullBlockESP(pathPos.getPos(), new Color(140, 3, 255, 100), event.getPartialTicks());
        } else { // Jump
          RenderUtil.drawFullBlockESP(pathPos.getPos(), new Color(255, 166, 0, 100), event.getPartialTicks());
        }
      }
    }
  }

  public void startRender(List<PathPos> path) {
    this.path = path;
    PathExec pathExec = new PathExec();
    pathExec.go(path);
    this.shortPath = pathExec.importantNodes;
  }

  public void clear() {
    this.path = null;
    this.shortPath = null;
  }
}
