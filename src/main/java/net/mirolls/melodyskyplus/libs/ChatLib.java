package net.mirolls.melodyskyplus.libs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ChatLib {
  public static void sendModMessage(String message) {
    String finalMessage = EnumChatFormatting.AQUA + "Melody Sky " + EnumChatFormatting.BLUE + "+" + EnumChatFormatting.AQUA + " > " + EnumChatFormatting.WHITE + message;

    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(finalMessage));
  }
}
