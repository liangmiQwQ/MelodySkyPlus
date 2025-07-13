package net.mirolls.melodyskyplus.libs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.mirolls.melodyskyplus.client.AntiBug;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiUtil {
  public static String getGuiName(GuiScreen gui) {
    if (AntiBug.isBugRemoved()) {
      return gui instanceof GuiChest ? ((ContainerChest) ((GuiChest) gui).inventorySlots).getLowerChestInventory().getDisplayName().getUnformattedText() : "";
    }
    return "";
  }

  public static void clickSlot(int slot, int button, int mode) {
    if (AntiBug.isBugRemoved()) {
      Minecraft mc = Minecraft.getMinecraft();
      mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot, button, mode, mc.thePlayer);
    }
  }

  public static int getHeat(String input) {
    if (AntiBug.isBugRemoved()) {
      Pattern pattern = Pattern.compile("^\\d+");
      Matcher matcher = pattern.matcher(input.trim());

      if (matcher.find()) {
        return Integer.parseInt(matcher.group());
      } else {
        throw new IllegalArgumentException("Error: No target heat found");
      }
    }
    return Integer.MIN_VALUE;
  }
}
