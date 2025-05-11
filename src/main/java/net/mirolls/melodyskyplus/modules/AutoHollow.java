package net.mirolls.melodyskyplus.modules;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.utils.BlockUtils;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.render.RenderUtil;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AutoHollow extends Module {
  public static AutoHollow INSTANCE;
  public Stage stage = Stage.DEFAULT;
  public boolean started = false;
  public List<BlockPos> stones = new ArrayList<>();

  public AutoHollow() {
    super("AutoHollow", ModuleType.Mining);
    this.setModInfo("Auto dig a hollow to use AutoGemstone. ");
    this.except();
  }

  public static AutoHollow getINSTANCE() {
    if (INSTANCE == null) {
      Iterator<Module> var2 = ModuleManager.modules.iterator();

      Module m;
      do {
        if (!var2.hasNext()) {
          return null;
        }

        m = var2.next();
      } while (m.getClass() != AutoHollow.class);

      INSTANCE = (AutoHollow) m;
    }
    return INSTANCE;
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    if (AutoRuby.getINSTANCE().wps.size() > 1) {
      for (BlockPos pos : stones) {
        RenderUtil.drawFullBlockESP(pos, new Color(8, 125, 13, 100), event.getPartialTicks());
      }
    }
  }

  @EventHandler
  public void onTick(EventPreUpdate event) {

  }

  public void start() {
    int index = AutoRuby.getINSTANCE().wps.indexOf(PlayerUtils.getPlayerLocation().down());
    if (index != -1) {
      // 存在
      stage = Stage.DEFAULT;

      Vec3d start = Vec3d.ofCenter(AutoRuby.getINSTANCE().wps.get(index));
      Vec3d eyes = new Vec3d(start.getX(), start.getY() + 0.5 + mc.thePlayer.getEyeHeight(), start.getZ());
      Vec3d end = Vec3d.ofCenter(AutoRuby.getINSTANCE().wps.get(index + 1));

      started = true;
      stones = filterAir(BlockUtils.getBlocksBetween(eyes, end));
    } else {
      Helper.sendMessage("Please stand on a point to start AutoHollow. ");
    }
  }

  public List<BlockPos> filterAir(List<BlockPos> pos) {
    return pos.stream().filter((e) ->
        mc.theWorld.getBlockState(e).getBlock() == Blocks.stone
    ).collect(Collectors.toList());
  }

  public enum Stage {
    DEFAULT,
    LEFT_CLICK_MINE,
    PACKET_MINE,
    WALK,
    FALLING
  }
}

