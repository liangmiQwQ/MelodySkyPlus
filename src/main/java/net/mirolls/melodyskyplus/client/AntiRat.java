package net.mirolls.melodyskyplus.client;

import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class AntiRat {
  // 人话 防破解

  private static final HashMap<String, String> ratLists = new HashMap<>();
  private static String ultimateRat = "";

  static {
    ratLists.put("net.mirolls.melodyskyplus.Verify", "_%net.mirolls.melodyskyplus.Verify%_MD5");
    ratLists.put("net.mirolls.melodyskyplus.client.AntiBug", "_%net.mirolls.melodyskyplus.client.AntiBug%_MD5");
    ratLists.put("net.mirolls.melodyskyplus.client.Bug", "_%net.mirolls.melodyskyplus.client.Bug%_MD5");
    ratLists.put("net.mirolls.melodyskyplus.modules.MelodyPlusModules", "_%net.mirolls.melodyskyplus.modules.MelodyPlusModules%_MD5");
    ratLists.put("net.mirolls.melodyskyplus.MelodySkyPlus", "_%net.mirolls.melodyskyplus.MelodySkyPlus%_MD5");
  }

  private static void llIIIl() {
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

  private static String IllIIl(String className) {
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
      llIIIl();
      return null;
    }
  }

  private static String llIlll() {
    try {
      // 获取类所在的 JAR 文件路径
      String jarPath = MelodySkyPlus.class.getProtectionDomain().getCodeSource().getLocation().getPath();

      // 如果路径是 JAR 文件
      if (jarPath.endsWith(".jar")) {
        File jarFile = new File(jarPath);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(jarFile)) {
          byte[] byteArray = new byte[1024];
          int bytesRead;
          while ((bytesRead = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesRead);
          }
        }

        byte[] md5Bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Bytes) {
          sb.append(String.format("%02x", b));
        }
        return sb.toString();
      } else {
        llIIIl();
        return "";
      }
    } catch (NoSuchAlgorithmException | IOException e) {
      llIIIl();
      return "";
    }
  }

  public static String antiRats(CallbackInfoReturnable<String> cir) {
    Set<String> keySets = ratLists.keySet();

    for (String keySet : keySets) {
      if (!Objects.equals(ratLists.get(keySet), IllIIl(keySet))) {
        llIIIl();
      }
    }

    if (ultimateRat == null || ultimateRat.isEmpty()) {
      BufferedReader in;
      StringBuilder response;
      try {
        in = IllllI();

        response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
          response.append(line);
        }

        in.close();
      } catch (IOException e) {
        llIIIl();
        throw new RuntimeException(e);
      }
      ultimateRat = response.toString();
    }

    if (!ultimateRat.equals(llIlll())) {
      llIIIl();
    }

    return cir.getReturnValue();
  }

  private static BufferedReader IllllI() throws IOException {
    /*URL link = new URL("https://mld-plus.lmfans.cn:443/rat/" + MelodySkyPlus.VERSION);
    HttpURLConnection connection = (HttpURLConnection) link.openConnection();
    connection.setRequestMethod("GET");

    // 设置请求头
    connection.setRequestProperty("Content-Type", "application/json");

    // 读取响应
    int responseCode = connection.getResponseCode();
    return new BufferedReader(new InputStreamReader(
        responseCode >= 200 && responseCode < 300
            ? connection.getInputStream()
            : connection.getErrorStream()));
  }*/

    try {
      Class<?> urlClass = Class.forName("java.net.URL");
      Constructor<?> urlConstructor = urlClass.getConstructor(String.class);
      Object urlObj = urlConstructor.newInstance("https://mld-plus.lmfans.cn:443/rat/" + MelodySkyPlus.VERSION);

      // 通过反射获取 openConnection 方法并调用
      Method openConnectionMethod = urlClass.getMethod("openConnection");
      Object connection = openConnectionMethod.invoke(urlObj);

      // 通过反射强转为 HttpURLConnection
      Class<?> httpURLConnectionClass = Class.forName("java.net.HttpURLConnection");
      if (!httpURLConnectionClass.isInstance(connection)) {
        throw new IllegalStateException("Not an HttpURLConnection");
      }

      // 设置请求方法
      Method setRequestMethod = httpURLConnectionClass.getMethod("setRequestMethod", String.class);
      setRequestMethod.invoke(connection, "GET");

      // 设置请求头
      Method setRequestProperty = httpURLConnectionClass.getMethod("setRequestProperty", String.class, String.class);
      setRequestProperty.invoke(connection, "Content-Type", "application/json");

      // 获取响应码
      Method getResponseCode = httpURLConnectionClass.getMethod("getResponseCode");
      int responseCode = (int) getResponseCode.invoke(connection);

      // 选择输入流
      Method getInputStream = httpURLConnectionClass.getMethod("getInputStream");
      Method getErrorStream = httpURLConnectionClass.getMethod("getErrorStream");
      InputStream stream = null;
      stream = (InputStream) (responseCode >= 200 && responseCode < 300
          ? getInputStream.invoke(connection)
          : getErrorStream.invoke(connection));


      // 通过反射创建 InputStreamReader
      Class<?> inputStreamReaderClass = Class.forName("java.io.InputStreamReader");
      Constructor<?> inputStreamReaderConstructor = inputStreamReaderClass.getConstructor(InputStream.class);
//      Object inputStreamReaderObj = inputStreamReaderConstructor.newInstance(stream);
      Reader inputStreamReaderObj = (Reader) inputStreamReaderConstructor.newInstance(stream);

      // 通过反射创建 BufferedReader
      Class<?> bufferedReaderClass = Class.forName("java.io.BufferedReader");
      Constructor<?> bufferedReaderConstructor = bufferedReaderClass.getConstructor(Reader.class);
      return (BufferedReader) bufferedReaderConstructor.newInstance(inputStreamReaderObj);
    } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException |
             InstantiationException e) {
      throw new RuntimeException(e);
    }
  }
}