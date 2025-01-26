package net.mirolls.melodyskyplus.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.mirolls.melodyskyplus.libs.LevenshteinDistance;
import net.mirolls.melodyskyplus.react.TalkWithYouReact;
import xyz.Melody.Utils.Helper;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TalkWithYouEvent {
  int talkingWithMeTicks = -1;
  int triggerTick = -1;
  String senderName = null;

  public TalkWithYouEvent() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onMentionedYourName(ClientChatReceivedEvent event) {
    Minecraft mc = Minecraft.getMinecraft();
    String message = event.message.getUnformattedText();
    String playerName = mc.thePlayer.getName();

    // 进行处理 去除:前面的部分
    String[] messageSlices = message.split(Pattern.quote(":"));

    if (!message.startsWith("[") || !message.contains(":") || message.startsWith("[NPC")) {
      // 系统提示 直接 return 进行过滤 避免浪费性能

      return;
    }

    String senderName = null;
    if (messageSlices[0].contains(playerName)) {
      // 自己发的消息 直接忽略 避免浪费性能
      Helper.sendMessage("消息被第二层过滤");
      return;
    } else {
      // 提取senderName
      String[] senderInformationSlices = messageSlices[0].split(Pattern.quote("]"));
      Matcher matcher = Pattern.compile("^[a-zA-Z0-9_]+$").matcher(senderInformationSlices[senderInformationSlices.length - 1]);
      while (matcher.find()) {
        String sender = matcher.group();
        if (senderName == null && sender.length() >= 3) {
          senderName = sender;
        } else {
          throw new RuntimeException("Found two sender");
        }
      }
      Helper.sendMessage("提取到了SenderName: " + senderName + "     MatchThing:" + senderInformationSlices[senderInformationSlices.length - 1]);
    }

    StringBuilder rawMessageBuilder = new StringBuilder();
    for (int i = 0; i < messageSlices.length; i++) {
      if (i != 0) {
        rawMessageBuilder.append(messageSlices[i]);
      }
      if (i < messageSlices.length - 1) {
        rawMessageBuilder.append(":");
      }
    }

    String rawMessage = rawMessageBuilder.toString();
    // 分词后再处理
    Matcher matcher = Pattern.compile("^[a-zA-Z0-9_]+$").matcher(rawMessage);
    boolean isTalkingWithMe = false;

    int threshold = playerName.length() > 11 ? 4 :
        playerName.length() > 6 ? 3 :
            playerName.length() > 5 ? 2 : 0;
    while (matcher.find()) {
      String word = matcher.group().toLowerCase();
      if (LevenshteinDistance.isFuzzyMatch(playerName.toLowerCase(), word, threshold)) {
        isTalkingWithMe = true;
        break;
      } else if (playerName.toLowerCase().contains(word)) {
        isTalkingWithMe = true;
        break;
      }
    }

    if (isTalkingWithMe) {
      // 触发了
      talkingWithMeTicks = 0;
      triggerTick = 40 + new Random().nextInt(40);
      this.senderName = senderName;
      Helper.sendMessage("触发了");
    } else {
      Helper.sendMessage("消息被第三层过滤");
    }
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (talkingWithMeTicks != -1) {
      talkingWithMeTicks++;

      if (talkingWithMeTicks == triggerTick) {
        // 时间一到
        TalkWithYouReact.replyMessage(this.senderName);
        talkingWithMeTicks = -1;
        triggerTick = -1;
      }
    }
  }
}
