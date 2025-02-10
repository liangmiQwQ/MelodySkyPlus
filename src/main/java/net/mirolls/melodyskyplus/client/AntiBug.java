package net.mirolls.melodyskyplus.client;
// 混淆前代码

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class AntiBug {
  // 人话: 验证

  /*
    验证逻辑 UUID -> mld-plus.lmfans.cn/bug/remove -> 解密, 解密JSON 获取日期、token ->
    mld-plus.lmfans.cn/bug/check -> 传入token 返回有效性和创建日期以及该账户 UUID
    -> 检查获取日期、UID是否相同
  */
  public static boolean removeBug(CallbackInfoReturnable<Boolean> cir) {
    // 进行基础的获取
    try {
      String bugID = Minecraft.getMinecraft().getSession().getProfile().getId().toString();
      BufferedReader in = getBufferedReader(bugID);
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = in.readLine()) != null) {
        response.append(line);
      }
      in.close();

      String passWord = new String(Base64.getDecoder().decode("TWVsb2R5K0xpYW5nTWlNaQ=="), StandardCharsets.UTF_8);
      String decryptedData = decrypt(response.toString(), passWord);

      MelodySkyPlus.antiBug = new Gson().fromJson(decryptedData, Bug.class);
      return cir.getReturnValue();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean isBugRemoved() {
    if (Objects.equals(MelodySkyPlus.antiBug.getBugID(), Minecraft.getMinecraft().getSession().getProfile().getId().toString())) {
//      return Objects.equals(MelodySkyPlus.antiBug.getBugType(), "true");
      return true;
    }
    return false;
  }

  private static BufferedReader getBufferedReader(String bugID) throws IOException {
    URL url = new URL("https://mld-plus.lmfans.cn:443/bug/remove/?bugid=" + bugID);
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

  /* private static String encrypt(String data, String key) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] encrypted = cipher.doFinal(data.getBytes());
    return Base64.getEncoder().encodeToString(encrypted);
  } */

  private static String decrypt(String encryptedData, String key) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
    cipher.init(Cipher.DECRYPT_MODE, secretKey);
    byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
    return new String(decrypted);
  }
}