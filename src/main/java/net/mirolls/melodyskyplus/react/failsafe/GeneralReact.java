package net.mirolls.melodyskyplus.react.failsafe;

import net.minecraft.client.Minecraft;

import java.util.Random;

public class GeneralReact extends React {

  public static void react(Run run, String message) {
    Minecraft mc = Minecraft.getMinecraft();

    long sleepTime = 5000;

    try {
      Thread.sleep((long) (sleepTime / 1.5));

      Random random = new Random();
      sendQuestionMessage(random, mc, message);

      Thread.sleep(sleepTime / 5);

      rotate(mc, run, sleepTime, random);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
