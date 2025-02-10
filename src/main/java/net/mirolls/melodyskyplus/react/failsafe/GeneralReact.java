package net.mirolls.melodyskyplus.react.failsafe;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.math.Rotation;

import java.util.Random;

public class GeneralReact extends React {

  public static void react(EntityPlayer fakePlayer, String fakePlayerCheckMessage) {
    Minecraft mc = Minecraft.getMinecraft();

    new Thread(() -> {
      long sleepTime = 5000;

      try {
        Thread.sleep(sleepTime / 2);

        MelodySkyPlus.rotationLib.setSpeedCoefficient(1F);
        MelodySkyPlus.rotationLib.startRotating();
        MelodySkyPlus.rotationLib.setTargetRotation(new Rotation(mc.thePlayer.rotationYaw, -90F));

        Thread.sleep((long) (sleepTime / 1.5));

        Random random = new Random();
        sendQuestionMessage(random, mc, fakePlayerCheckMessage);

        Thread.sleep(sleepTime / 5);

        rotate(mc, () -> MathUtil.distanceToEntity(mc.thePlayer, fakePlayer) < 50, sleepTime, random);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
  }


}
