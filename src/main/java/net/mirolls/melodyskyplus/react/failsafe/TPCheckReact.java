package net.mirolls.melodyskyplus.react.failsafe;

public class TPCheckReact {
  public static void react(String tpCheckMessage) {
    GeneralReact.react(() -> true, tpCheckMessage);
  }
}
