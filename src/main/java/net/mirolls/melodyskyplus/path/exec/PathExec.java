package net.mirolls.melodyskyplus.path.exec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.mirolls.melodyskyplus.modules.SmartyPathFinder;
import net.mirolls.melodyskyplus.path.type.*;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.Event.EventBus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.Helper;

import java.util.List;
import java.util.Objects;

public class PathExec {

  public static SkyblockArea area = null;
  public AbilityExec abilityExec = new AbilityExec();

  public PathExec() {
    EventBus.getInstance().register(this);
    MinecraftForge.EVENT_BUS.register(this);
  }


  @EventHandler
  public void onTick(EventPreUpdate event) {
    SmartyPathFinder smartyPathFinder = Objects.requireNonNull(SmartyPathFinder.getINSTANCE());
    Minecraft mc = Minecraft.getMinecraft();

    List<Node> path = smartyPathFinder.path;

    if (path != null && !path.isEmpty()) {
      if (path.size() == 1) {
        // 走到终点自动停止
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        smartyPathFinder.clear();
        area = null;
        abilityExec = new AbilityExec();
        return;
      }

      if (area == null) {
        area = new SkyblockArea();
        area.updateCurrentArea();
        Helper.sendMessage(area.isIn(Areas.Crystal_Hollows) ? "Since you're in Crystal Hollows, you will use C07Packet to mine" : "Since you're not in Crystal Hollows, you will use left click to mine");
      }

      // 执行器核心 运行path
      Node node = path.get(0);
      Node nextNode = path.get(1);

      if (abilityExec.rubbish) {
        abilityExec = new AbilityExec();
      }

      if (nextNode instanceof Walk) {
        WalkExec.exec(nextNode, mc, node);

        // 这里给停止部分单独拉出来 是为了让Jump的代码复用
        if (Math.abs(PlayerUtils.getPlayerLocation().getX() - nextNode.getPos().getX()) <= 1 && Math.abs(PlayerUtils.getPlayerLocation().getZ() - nextNode.getPos().getZ()) <= 1) {
          path.remove(0);
        }
      } else if (nextNode instanceof Mine) {
        MineExec.exec(nextNode, mc, path, smartyPathFinder, area
        );
      } else if (nextNode instanceof Jump) {
        JumpExec.exec(nextNode, path, mc, node);
      } else if (nextNode instanceof Ability) {
        abilityExec.exec(nextNode, path, mc, smartyPathFinder, node);
      }
    }
  }
}
