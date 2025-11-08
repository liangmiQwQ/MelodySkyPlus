package net.mirolls.melodyskyplus.react.failsafe;

import xyz.Melody.Utils.Helper;

public class TPCheckReact {
  public static void react(String tpCheckMessage) {
    Helper.sendMessage("Staff checked you with TP, start to react.");
    new Thread(
            () -> {
              GeneralReact.react(() -> true, tpCheckMessage);
            })
        .start();
  }
}
