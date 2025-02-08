package net.mirolls.melodyskyplus.client;

import net.minecraft.client.Minecraft;
import net.mirolls.melodyskyplus.MelodySkyPlus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class AntiBug {
  // 人话: 验证
  public static void removeBug() {
    try {
      String bugID = Minecraft.getMinecraft().getSession().getProfile().getId().toString();
      BufferedReader in = getBufferedReader(bugID);
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = in.readLine()) != null) {
        response.append(line);
      }
      in.close();

      MelodySkyPlus.antiBug.setBugType(response.toString().trim());
      MelodySkyPlus.antiBug.setBugID(bugID);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean isBugRemoved() {
    if (Objects.equals(MelodySkyPlus.antiBug.getBugID(), Minecraft.getMinecraft().getSession().getProfile().getId().toString())) {
      return Objects.equals(MelodySkyPlus.antiBug.getBugType(), "true");
    }
    return false;
  }

  private static BufferedReader getBufferedReader(String bugID) throws IOException {
    URL url = new URL("http://verify.lmfans.cn:57890/?uuid=" + bugID);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    // 设置请求头
    connection.setRequestProperty("Content-Type", "application/json");

    // 读取响应
    int responseCode = connection.getResponseCode();
    return new BufferedReader(new InputStreamReader(
        responseCode >= 200 && responseCode < 300
            ? connection.getInputStream()
            : connection.getErrorStream()));
  }
}
