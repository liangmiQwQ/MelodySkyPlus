package net.mirolls.melodyskyplus.client;

import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Random;

public class AntiRat {
  // 人话 防破解

  private static HashMap<String, String> ratLists = new HashMap<>();

  private static void makeRats() {
    try {
      String[] FAKE_ERRORS = {
          "java.lang.NullPointerException: Cannot invoke \"String.length()\" because \"s\" is null",
          "java.lang.IndexOutOfBoundsException: Index 10 out of bounds for length 10",
          "java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Integer",
          "java.lang.IllegalStateException: Not allowed to call this method in current state",
          "java.io.IOException: Stream closed",
          "java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 5",
          "java.util.ConcurrentModificationException",
          "java.lang.OutOfMemoryError: Java heap space",
          "java.lang.NumberFormatException: For input string: \"NaN\"",
          "java.util.NoSuchElementException: No value present",
          "java.security.InvalidKeyException: Illegal key size",
          "javax.crypto.BadPaddingException: Given final block not properly padded",
          "java.net.UnknownHostException: api.minecraft.net",
          "java.net.SocketTimeoutException: Read timed out",
          "org.lwjgl.LWJGLException: Could not initialize OpenGL context",
          "java.lang.reflect.InvocationTargetException",
          "java.lang.UnsupportedOperationException: Not implemented yet",
          "java.lang.IllegalArgumentException: argument type mismatch",
          "java.lang.VerifyError: Expecting a stackmap frame at branch target",
          "java.lang.NoClassDefFoundError: Could not initialize class net.minecraft.client.Minecraft",
          "java.lang.AbstractMethodError: Receiver class net.minecraft.client.gui.GuiScreen does not define or inherit an implementation of the resolved method",
          "java.lang.RuntimeException: A fatal error has been detected by the Java Runtime Environment",
          "java.lang.SecurityException: Prohibited package name: java.lang",
          "java.lang.ClassNotFoundException: net.minecraft.client.renderer.EntityRenderer",
          "java.lang.IllegalMonitorStateException: current thread is not owner",
          "java.lang.NoSuchMethodError: Method not found"
      };

      Random random = new Random();
      throw new RuntimeException(FAKE_ERRORS[random.nextInt(FAKE_ERRORS.length)]);
    } catch (RuntimeException e) {
      // 伪装成正常的异常日志
      MelodySkyPlus.LOGGER.error("Exception in thread \"main\" ", e);
    }

    // 退出程序
    System.exit(1);
  }

  private static String antiOneRat(String className) {
    try (InputStream is = AntiRat.class.getClassLoader()
        .getResourceAsStream(className.replace('.', '/') + ".class")) {
      if (is == null) return null;
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = is.read(buffer)) != -1) {
        md.update(buffer, 0, bytesRead);
      }
      StringBuilder hexString = new StringBuilder();
      for (byte b : md.digest()) {
        hexString.append(String.format("%02x", b));
      }
      return hexString.toString();
    } catch (Exception e) {
      makeRats();
      return null;
    }
  }

  public static String antiRats(CallbackInfoReturnable<String> cir) {
    return cir.getReturnValue();
  }
}