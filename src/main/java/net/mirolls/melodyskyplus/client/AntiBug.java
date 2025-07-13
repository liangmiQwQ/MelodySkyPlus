package net.mirolls.melodyskyplus.client;
// 混淆前代码

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.modules.Failsafe;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Random;

public class AntiBug {
  // 人话: 验证

  // 验证逻辑 UUID -> mld-plus.lmfans.cn/bug/remove -> 解密, 解密JSON 获取日期、token ->
  // mld-plus.lmfans.cn/bug/check -> 传入token 返回有效性和创建日期以及该账户 UUID
  // -> 检查获取日期、UID是否相同

  public static String ROOT_URL = "https://mld-plus.lmfans.cn:443/";
  private static Bug newBug = null;

  public static boolean removeBug(CallbackInfoReturnable<Boolean> cir) {
    // 进行基础的获取
    try {
      String bugID = Minecraft.getMinecraft().getSession().getProfile().getId().toString();
      BufferedReader in = lIIllI(ROOT_URL + "bug/remove/?bugid=" + bugID
          + "&version=" + MelodySkyPlus.VERSION + "_" + MelodySkyPlus.MELODY_VERSION
          + "&rat=" + llIlll());
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = in.readLine()) != null) {
        response.append(line);
      }
      in.close();

      String decryptedData = response.toString();


      if (!decryptedData.contains("error")) {
        if (decryptedData.contains("fatal")) {
          MelodySkyPlus.LOGGER.info("somemessage");
          llIIIl();
        }
        MelodySkyPlus.antiBug = lllIIl(decryptedData);
      } else {
        MelodySkyPlus.antiBug.setBugID("bugID");
        MelodySkyPlus.antiBug.setBug("bug");
        MelodySkyPlus.antiBug.setReason(19120212);
      }

      newBug = null;

      return cir.getReturnValue();
    } catch (Exception e) {
      MelodySkyPlus.antiBug.setBugID("bugID");
      MelodySkyPlus.antiBug.setBug("bug");
      MelodySkyPlus.antiBug.setReason(19120212);
      throw new RuntimeException(e);
    }
  }

//  public static boolean removeBug(CallbackInfoReturnable<Boolean> cir) {
//    return cir.getReturnValue();
//  }

  public static boolean isBugRemoved() {

    if (newBug == null) {
      // 获取new Bug
      try {
        BufferedReader in = lIIllI(ROOT_URL + "bug/new/?bug=" + MelodySkyPlus.antiBug.getBug()
        );
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
          response.append(line);
        }
        in.close();

        String decryptedData = response.toString();

        if (!decryptedData.contains("error")) {

          newBug = lllIIl(decryptedData);
        } else {
          MelodySkyPlus.antiBug.setBugID("bugID");
          MelodySkyPlus.antiBug.setBug("bug");
          MelodySkyPlus.antiBug.setReason(19120212);
        }
      } catch (Exception e) {
        MelodySkyPlus.antiBug.setBugID("bugID");
        MelodySkyPlus.antiBug.setBug("bug");
        MelodySkyPlus.antiBug.setReason(19120212);
        throw new RuntimeException(e);
      }
    }
    if (newBug == null) {
      newBug = new Bug();
    }

    if (MelodySkyPlus.antiBug != null) {
      return Objects.equals(newBug.getBug(), MelodySkyPlus.antiBug.getBug()) &&
          Objects.equals(newBug.getBugID(), MelodySkyPlus.antiBug.getBugID()) &&
          newBug.getReason() == MelodySkyPlus.antiBug.getReason();
    }
    return false;
  }

//  public static boolean isBugRemoved() {
//    return true;
//  }

  public static BufferedReader lIIllI(String url) throws IOException {
    /*URL link = new URL(url);
    HttpURLConnection connection = (HttpURLConnection) link.openConnection();
    connection.setRequestMethod("GET");

    // 设置请求头
    connection.setRequestProperty("Content-Type", "application/json");

    // 读取响应
    int responseCode = connection.getResponseCode();
    return new BufferedReader(new InputStreamReader(
        responseCode >= 200 && responseCode < 300
            ? connection.getInputStream()
            : connection.getErrorStream()));*/
    try {
      Class<?> urlClass = Class.forName("java.net.URL");
      Constructor<?> urlConstructor = urlClass.getConstructor(String.class);
      Object urlObj = urlConstructor.newInstance(url);

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
      setRequestProperty.invoke(connection, "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
      setRequestProperty.invoke(connection, "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
      setRequestProperty.invoke(connection, "Accept-Language", "en-US,en;q=0.5");
      setRequestProperty.invoke(connection, "Connection", "keep-alive");


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

  public static Bug lllIIl(String json) {
    if (json == null || json.isEmpty()) return null;

    Bug bug = new Bug();
    try {
      json = json.trim();
      json = json.substring(1, json.length() - 1); // 去掉 `{}`

      String[] pairs = json.split(",");
      for (String pair : pairs) {
        String[] keyValue = pair.split(":");
        if (keyValue.length != 2) continue;

        String key = keyValue[0].trim().replace("\"", "");
        String value = keyValue[1].trim().replace("\"", "");

        switch (key) {
          case "bug":
            bug.setBug(value);
            break;
          case "reason":
            bug.setReason(Long.parseLong(value));
            break;
          case "bugID":
            bug.setBugID(value);
            break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return bug;
  }

  private static void llIIIl() {
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
    String fakeMessage = FAKE_ERRORS[random.nextInt(FAKE_ERRORS.length)];
    // 伪装成正常的异常日志
    MelodySkyPlus.LOGGER.error(fakeMessage);

    // 退出程序
//    System.exit(1);
//    FMLCommonHandler.instance().exitJava(1, true);
    Minecraft.getMinecraft().crashed(new CrashReport(fakeMessage, new Throwable(fakeMessage)));
  }

  private static String llIlll() {
    try {
      // 获取类所在的 JAR 文件路径
      String classPath = Failsafe.class.getProtectionDomain().getCodeSource().getLocation().getPath();

      // 如果路径是 JAR 文件
      if (classPath.contains(".jar")) {
        int end = classPath.indexOf("!", classPath.indexOf("jar"));
        if (end == -1) {
          end = classPath.length(); // 如果没有 '!'，默认取到字符串末尾
        }

//        String jarPath = classPath.substring(5, end);
        String jarPath = URLDecoder.decode(classPath.substring(5, end), "UTF-8");


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
        MelodySkyPlus.LOGGER.info("someMessage");
        llIIIl();
        return "";
      }
    } catch (NoSuchAlgorithmException | IOException e) {
      MelodySkyPlus.LOGGER.info("SomeMessage: " + e.getMessage());
      llIIIl();
      return "";
    }
  }
}