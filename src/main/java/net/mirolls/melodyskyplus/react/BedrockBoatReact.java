package net.mirolls.melodyskyplus.react;

import net.minecraft.client.Minecraft;
import xyz.Melody.Utils.Helper;

public class BedrockBoatReact {


  public static void react() {
    Helper.sendMessage("Staff checked you with bedrock boat, start to react.");

    Minecraft mc = Minecraft.getMinecraft();

    new Thread(() -> {
      long sleepTime = 5000;

      try {
        Thread.sleep(sleepTime / 2);


      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }


    }).start();
  }
}

