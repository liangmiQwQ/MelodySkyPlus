package net.mirolls.melodyskyplus.path;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraftforge.common.MinecraftForge;
import net.mirolls.melodyskyplus.modules.SmartyPathFinder;
import net.mirolls.melodyskyplus.path.find.PathPos;
import net.mirolls.melodyskyplus.path.type.Node;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.render.ColorUtils;
import xyz.Melody.Utils.render.RenderUtil;

public class PathRenderer {
  public PathRenderer() {
    EventBus.getInstance().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    SmartyPathFinder smartyPathFinder = Objects.requireNonNull(SmartyPathFinder.getINSTANCE());
    if (smartyPathFinder.path != null && smartyPathFinder.aStarPath != null) {
      if (!smartyPathFinder.path.isEmpty() && !smartyPathFinder.aStarPath.isEmpty()) {
        for (PathPos pathPos : smartyPathFinder.aStarPath) {
          if (pathPos.getType() == PathPos.PathNodeType.WALK) {
            RenderUtil.drawFullBlockESP(
                pathPos.getPos(),
                ColorUtils.transparency(new Color(8, 125, 13).getRGB(), 0.4),
                event.getPartialTicks());
          } else if (pathPos.getType() == PathPos.PathNodeType.MINE) {
            RenderUtil.drawFullBlockESP(
                pathPos.getPos(),
                ColorUtils.transparency(new Color(189, 2, 13).getRGB(), 0.4),
                event.getPartialTicks());
          } else if (pathPos.getType() == PathPos.PathNodeType.ABILITY_START) {
            RenderUtil.drawFullBlockESP(
                pathPos.getPos(),
                ColorUtils.transparency(new Color(37, 200, 255).getRGB(), 0.4),
                event.getPartialTicks());
          } else if (pathPos.getType() == PathPos.PathNodeType.ABILITY_END) {
            RenderUtil.drawFullBlockESP(
                pathPos.getPos(),
                ColorUtils.transparency(new Color(140, 3, 255).getRGB(), 0.4),
                event.getPartialTicks());
          } else if (pathPos.getType() == PathPos.PathNodeType.JUMP_END) {
            RenderUtil.drawFullBlockESP(
                pathPos.getPos(),
                ColorUtils.transparency(new Color(255, 166, 0).getRGB(), 0.4),
                event.getPartialTicks());
          } else if (pathPos.getType() == PathPos.PathNodeType.ABILITY_BETWEEN) {
            RenderUtil.drawFullBlockESP(
                pathPos.getPos(),
                ColorUtils.transparency(new Color(14, 107, 210).getRGB(), 0.4),
                event.getPartialTicks());
          }
        }

        RenderUtil.drawLines(
            (ArrayList<Vec3d>) Node.toVec3dArray(smartyPathFinder.path),
            5.0F,
            event.getPartialTicks());
      }
    }
  }
}
