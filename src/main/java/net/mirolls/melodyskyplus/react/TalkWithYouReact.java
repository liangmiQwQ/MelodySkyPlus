package net.mirolls.melodyskyplus.react;


import net.minecraft.client.Minecraft;

import java.util.Random;

public class TalkWithYouReact {
  /**
   * 回复玩家消息 表达自己不懂英语 要用其他语言
   *
   * @param playerName 要回复的玩家名称
   */
  public static void replyMessage(String playerName) {
    StringBuilder badEnglishSentence = new StringBuilder();

    // 核心
    Random random = new Random();

    int sentenceNumber = random.nextInt(3);
    String speakWord = null;


    // 句式1 标准写法: I'm not good at English.
    if (sentenceNumber == 0) {
      // 添加主语
      String[] firstPersonSubjects = new String[]{"i ", "me ", "i'm ", "i am ", "me is ", "i is "};
      String firstPersonSubject = firstPersonSubjects[random.nextInt(firstPersonSubjects.length)];
      if (random.nextBoolean()) {
        badEnglishSentence.append(String.format("%s%s", firstPersonSubject.substring(0, 1).toUpperCase(), firstPersonSubject.substring(1)));
      } else {
        badEnglishSentence.append(firstPersonSubject);
      }

      // 添加一些形容词等修饰
      String[] noGoodEnglishWords = new String[]{"bad ", "not good at ", "bed ", "no good at ", "noGood ", "ungood ", "no good at ", "bad at ", "ungood at "};
      badEnglishSentence.append(noGoodEnglishWords[random.nextInt(noGoodEnglishWords.length)]);

      // 添加一下english
      if (random.nextBoolean()) {
        badEnglishSentence.append("English");
      } else {
        badEnglishSentence.append("english");
      }

      // 句点
      if (random.nextBoolean()) {
        badEnglishSentence.append(".");
      }
    } else if (sentenceNumber == 1) {
      // 情况二 句式: My English is not good
      // 添加主语
      String[] firstPersonSubjects = new String[]{"my ", "me ", "i's ", "me is ", "i is "};
      String firstPersonSubject = firstPersonSubjects[random.nextInt(firstPersonSubjects.length)];
      if (random.nextBoolean()) {
        badEnglishSentence.append(String.format("%s%s", firstPersonSubject.substring(0, 1).toUpperCase(), firstPersonSubject.substring(1)));
      } else {
        badEnglishSentence.append(firstPersonSubject);
      }

      // 添加一下english
      if (random.nextBoolean()) {
        badEnglishSentence.append("English ");
      } else {
        badEnglishSentence.append("english ");
      }

      // 添加一些形容词等修饰
      String[] noGoodEnglishWords = new String[]{"is bad", "is not good", "bed", "is no good at", "is noGood", "ungood", "no good", "bad", "is ungood"};
      badEnglishSentence.append(noGoodEnglishWords[random.nextInt(noGoodEnglishWords.length)]);

      if (random.nextBoolean()) {
        badEnglishSentence.append(".");
      }
    } else {
      // 句式三: I can't speak English well
      String[] firstPersonSubjects = new String[]{"i can't ", "me can't ", "i cant ", "me cant "};
      String firstPersonSubject = firstPersonSubjects[random.nextInt(firstPersonSubjects.length)];
      if (random.nextBoolean()) {
        badEnglishSentence.append(String.format("%s%s", firstPersonSubject.substring(0, 1).toUpperCase(), firstPersonSubject.substring(1)));
      } else {
        badEnglishSentence.append(firstPersonSubject);
      }

      // speak
      String[] speakWords = new String[]{"say ", "speak ", "read ", "learn ", "spaek ", "sya "};
      speakWord = speakWords[random.nextInt(speakWords.length)];
      badEnglishSentence.append(speakWord);

      // 添加一下english
      if (random.nextBoolean()) {
        badEnglishSentence.append("English");
      } else {
        badEnglishSentence.append("english");
      }

      // 添加一些形容词等修饰
      String[] noGoodEnglishWords = new String[]{"well", "good", "nice", "great"};
      badEnglishSentence.append(noGoodEnglishWords[random.nextInt(noGoodEnglishWords.length)]);

      // 句点
      if (random.nextBoolean()) {
        badEnglishSentence.append(".");
      } else {
        badEnglishSentence.append(" ");
      }
    }

    if (random.nextBoolean()) {
      badEnglishSentence.append(" ");
    }

    StringBuilder adviseMessageBuilder = new StringBuilder();
    if (random.nextBoolean()) {
      // 推荐他说其他语言
      // 句式1 Can you speak Chinese
      if (random.nextBoolean()) {
        // 句式三: I can't speak English well
        String[] askingWords = new String[]{"can ", "may "};
        String askingWord = askingWords[random.nextInt(askingWords.length)];
        if (random.nextBoolean()) {
          adviseMessageBuilder.append(String.format("%s%s", askingWord.substring(0, 1).toUpperCase(), askingWord.substring(1)));
        } else {
          adviseMessageBuilder.append(askingWord);
        }
      } else {
        // 句式2 You Can Speak Chinese / please speak English
        String[] firstPersonSubjects = new String[]{"you can ", "u can ", "please ", "u need ", "you need to "};
        String firstPersonSubject = firstPersonSubjects[random.nextInt(firstPersonSubjects.length)];
        if (random.nextBoolean()) {
          adviseMessageBuilder.append(String.format("%s%s", firstPersonSubject.substring(0, 1).toUpperCase(), firstPersonSubject.substring(1)));
        } else {
          adviseMessageBuilder.append(firstPersonSubject);
        }
      }

      if (speakWord == null) {
        String[] speakWords = new String[]{"say ", "speak ", "read ", "learn ", "spaek ", "sya "};
        speakWord = speakWords[random.nextInt(speakWords.length)];
      }
      adviseMessageBuilder.append(speakWord);

      // 添加一下english
      if (random.nextBoolean()) {
        adviseMessageBuilder.append("English ");
      } else {
        adviseMessageBuilder.append("english ");
      }
    }


    String finalMessage;
    if (random.nextBoolean()) {
      finalMessage = adviseMessageBuilder.append(badEnglishSentence).toString();
    } else {
      finalMessage = badEnglishSentence.append(adviseMessageBuilder).toString();
    }

    if (random.nextBoolean()) {
      String[] sorryWords = new String[]{"Sorry. ", "sry ", "sorry, ", "sorry, "};
      String sorryWord = sorryWords[random.nextInt(sorryWords.length)];
      if (random.nextBoolean()) {
        finalMessage = sorryWord + finalMessage;
      } else {
        finalMessage = finalMessage + sorryWord;

      }
    }

    Minecraft mc = Minecraft.getMinecraft();

    mc.thePlayer.sendChatMessage(finalMessage);
  }
}
