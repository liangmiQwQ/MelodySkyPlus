package net.mirolls.melodyskyplus.modules;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.utils.BlockUtils;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.render.RenderUtil;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;

import java.awt.*;

public class AutoHollow extends Module {
  public Stage stage = Stage.DEFAULT;

  public AutoHollow() {
    super("AutoHollow", ModuleType.Mining);
    this.setModInfo("Auto dig a hollow to use AutoGemstone. ");
    this.except();
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    if (AutoRuby.getINSTANCE().wps.size() > 1) {
      Vec3d start = Vec3d.ofCenter(AutoRuby.getINSTANCE().wps.get(0));
      Vec3d eyes = new Vec3d(start.getX(), start.getY() + 0.5 + mc.thePlayer.getEyeHeight(), start.getZ());
      Vec3d end = Vec3d.ofCenter(AutoRuby.getINSTANCE().wps.get(1));

      for (BlockPos pos : BlockUtils.getBlocksBetween(eyes, end)) {
        RenderUtil.drawFullBlockESP(pos, new Color(8, 125, 13, 100), event.getPartialTicks());
      }
    }
  }

  @EventHandler
  public void onTick(EventPreUpdate event) {

  }

  public enum Stage {
    DEFAULT,
    LEFT_CLICK_MINE,
    PACKET_MINE,
    WALK,
    FALLING
  }
}

