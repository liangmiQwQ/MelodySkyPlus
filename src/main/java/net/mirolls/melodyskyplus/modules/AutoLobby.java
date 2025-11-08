package net.mirolls.melodyskyplus.modules;

import java.util.Random;
import net.mirolls.melodyskyplus.client.ModulePlus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.ModuleType;

public class AutoLobby extends ModulePlus {
  public final Option<Boolean> useWarpCn = new Option<>("/warp cn", true);
  public final Numbers<Double> minDay = new Numbers<>("MinDay", 0.0, 0.0, 18.0, 1.0);
  public final Numbers<Double> maxDay = new Numbers<>("MaxDay", 5.0, 0.0, 18.0, 1.0);

  private final TimerUtil warpTimer = new TimerUtil();

  public AutoLobby() {
    super("AutoLobby", ModuleType.Mining);
    this.addValues(useWarpCn, minDay, maxDay);
    this.setModInfo("Auto find Crystal Hollow lobbies with a proper day. ");
    this.except();
  }

  @Override
  public void onEnable() {
    super.onEnable();
    Helper.sendMessage("AutoLobby has been enabled. Trying to find a good lobby to mine.");
    warpTimer.reset();
  }

  @Override
  public void onDisable() {
    super.onDisable();
    warpTimer.reset();
  }

  @EventHandler
  public void onTick(EventTick event) {
    if (warpTimer.hasReached(5 * 1000 + new Random().nextInt(1000))) {
      warpTimer.reset();

      SkyblockArea area = new SkyblockArea();
      area.updateCurrentArea();

      if (area.isIn(Areas.NULL)) return;

      if (area.isIn(Areas.Crystal_Hollows)) {
        long day = mc.theWorld.getWorldTime() / 24000L;
        if (minDay.getValue() <= day && day <= maxDay.getValue()) {
          // 找到大厅了
          Helper.sendMessage("Found a lobby with a good day. Auto disable");
          this.setEnabled(false);
        } else {
          // 回岛屿
          mc.thePlayer.sendChatMessage("/is");
        }
      } else {
        mc.thePlayer.sendChatMessage(useWarpCn.getValue() ? "/warp cn" : "/warp ch");
      }
    }
  }
}
