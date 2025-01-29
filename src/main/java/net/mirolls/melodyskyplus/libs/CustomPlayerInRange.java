package net.mirolls.melodyskyplus.libs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import xyz.Melody.System.Managers.Client.FriendManager;
import xyz.Melody.Utils.game.PlayerListUtils;
import xyz.Melody.Utils.math.MathUtil;

public class CustomPlayerInRange {
  public static Object[] redirectPlayerInRange(boolean checkRange, double range, boolean reqSee) {
    Minecraft mc = Minecraft.getMinecraft();
    short scanTicks = PrivateFieldGetter.get("xyz.Melody.Utils.AFKUtils", "scanTicks", Short.class);

    if (scanTicks % 10 != 0) {
      return new Object[]{false, "NOT_THIS"};
    } else {
      boolean isTherePlayer = false;
      String fakePlayerName = "NOT_THIS";
      String name = "";

      for (EntityPlayer ep : mc.theWorld.playerEntities) {
        String n = ep.getName().toLowerCase();
        if (!n.contains("kalhuki tribe member") && !n.contains("weakling") && !n.contains("goblin")
            && !n.contains("team treasurite") &&
            !FriendManager.isFriend(ep.getName()) && !FriendManager.isFriend(ep.getDisplayNameString()) && ep != mc.thePlayer) {
          if ((!reqSee || mc.thePlayer.canEntityBeSeen(ep)) && (!checkRange || (double) MathUtil.distanceToEntity(mc.thePlayer, ep) < range)) {
            if (PlayerListUtils.isInTablist(ep)) {
              isTherePlayer = true;
              name = ep.getName();
              break;
            } else {
              // fakePlayer
              fakePlayerName = ep.getName();
            }
          }
        }
      }

      return new Object[]{isTherePlayer, name, fakePlayerName};
    }
  }
}