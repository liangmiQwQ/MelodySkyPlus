package net.mirolls.melodyskyplus.react.failsafe;

import net.minecraft.client.Minecraft;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.math.Rotation;

public class FakePlayerCheckReact {
  public static void react(Run run, String message) {
    Helper.sendMessage("Staff checked you with FakePlayer, start to react.");

    new Thread(
            () -> {
              try {
                Thread.sleep(1500);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }

              MelodySkyPlus.rotationLib.setSpeedCoefficient(1F);
              MelodySkyPlus.rotationLib.startRotating();
              MelodySkyPlus.rotationLib.setTargetRotation(
                  new Rotation(Minecraft.getMinecraft().thePlayer.rotationYaw, -90F));

              GeneralReact.react(run, message);
            })
        .start();
  }
}
