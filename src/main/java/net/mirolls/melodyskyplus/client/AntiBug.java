package net.mirolls.melodyskyplus.client;
// 混淆前代码

import net.minecraft.client.Minecraft;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class AntiBug {
  // 人话: 验证

  // 验证逻辑 UUID -> mld-plus.lmfans.cn/bug/remove -> 解密, 解密JSON 获取日期、token ->
  // mld-plus.lmfans.cn/bug/check -> 传入token 返回有效性和创建日期以及该账户 UUID
  // -> 检查获取日期、UID是否相同

  private static Bug newBug = null;

  public static boolean removeBug(CallbackInfoReturnable<Boolean> cir) {
    // 进行基础的获取
    try {
      String bugID = Minecraft.getMinecraft().getSession().getProfile().getId().toString();
      BufferedReader in = lIIllI("https://mld-plus.lmfans.cn:443/bug/remove/?bugid=" + bugID);
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = in.readLine()) != null) {
        response.append(line);
      }
      in.close();

      String decryptedData = response.toString();

      if (!decryptedData.contains("error")) {
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
        BufferedReader in = lIIllI("https://mld-plus.lmfans.cn:443/bug/new/?bug=" + MelodySkyPlus.antiBug.getBug());
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

  private static BufferedReader lIIllI(String url) throws IOException {
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
}