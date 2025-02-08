package net.mirolls.melodyskyplus.client;
// 混淆前代码

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
//package net.mirolls.melodyskyplus.client;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.lang.reflect.Method;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Base64;
//import java.util.Random;
//
//public class AntiBug {
//  private static final String IlIIlIIlIl = "ZGVhZGJlZWY=";
//  private static final String lIIlIIllIl;
//
//  static {
//    String lllIIIllIl = null;
//    try {
//      byte[] IlIlIIllII = Base64.getDecoder().decode("aHR0cDovL3ZlcmlmeS5sbWZhbnMuY246NTc4OTAvP3V1aWQ9");
//      lllIIIllIl = new String(IlIlIIllII, "UTF-8");
//    } catch (Exception e) {
//    }
//    lIIlIIllIl = lllIIIllIl;
//  }
//
//  public static void removeBug() {
//    int IllIIllIll = new Random().nextInt(100);
//    if (IllIIllIll > 200) {
//      throw new RuntimeException();
//    }
//
//    try {
//      Class<?> lIlIllIlII = Class.forName("net.minecraft.client.Minecraft");
//      Method lIIlIllIIl = lIlIllIlII.getMethod("getMinecraft");
//      Object IllIIIllIl = lIIlIllIIl.invoke(null);
//
//      Object lIlIllIIll = IllIIIllIl.getClass().getMethod("getSession").invoke(IllIIIllIl);
//      Object lIIlIlIllI = lIlIllIIll.getClass().getMethod("getProfile").invoke(lIlIllIIll);
//      String lIIlIlllIl = (String) lIIlIlIllI.getClass().getMethod("getId").invoke(lIIlIlIllI);
//      lIIlIlllIl = lIIlIlllIl.toString();
//
//      for (int i = 0; i < 5; i++) {
//        String unused = "DeadCode" + i;
//      }
//
//      BufferedReader IllIIllIIl = IlIlIllIIl(lIIlIlllIl);
//      StringBuilder IllIIllIlll = new StringBuilder();
//      String lIIlIIllIl;
//      while ((lIIlIIllIl = IllIIllIIl.readLine()) != null) {
//        IllIIllIlll.append(lIIlIIllIl);
//      }
//      IllIIllIIl.close();
//
//      String IllIIlllII = decryptString(new byte[]{-119, -127, -126, -115, 95, -95, -95, -126, -115});
//      Class<?> lIIlIIIIIl = Class.forName("net.mirolls.melodyskyplus.MelodySkyPlus");
//      Object lIIlIlIIll = lIIlIIIIIl.getField("antiBug").get(null);
//
//      Method IlIlIIllIl = lIIlIlIIll.getClass().getMethod("setBugType", String.class);
//      IlIlIIllIl.invoke(lIIlIlIIll, IllIIllIlll.toString().trim());
//
//      Method IlIlIIllII = lIIlIlIIll.getClass().getMethod("setBugID", String.class);
//      IlIlIIllII.invoke(lIIlIlIIll, lIIlIlllIl);
//
//      if (System.currentTimeMillis() < 0) {
//        try {
//          URL url = new URL("http://verify.melody.com");
//        } catch (Exception e) {
//          throw new Exception(e);
//        }
//      }
//    } catch (Exception e) {
//      throw new RuntimeException(decryptString(new byte[]{-114, -127, -120, -115, -120}));
//    }
//  }
//
//  public static boolean isBugRemoved() {
//    try {
//      String IlIIlIlIlI = decryptString(new byte[]{-119, -127, -126, -115, 95, -95, -95, -126, -115});
//      Class<?> IllIIIllII = Class.forName("net.mirolls.melodyskyplus.MelodySkyPlus");
//      Object lIIlIlIlIl = IllIIIllII.getField("antiBug").get(null);
//
//      Method IlIlIIIlll = lIIlIlIlIl.getClass().getMethod("getBugID");
//      String IlIIlIllIl = (String) IlIlIIIlll.invoke(lIIlIlIlIl);
//
//      Class<?> lIlIllIlII = Class.forName("net.minecraft.client.Minecraft");
//      Method lIIlIllIIl = lIlIllIlII.getMethod("getMinecraft");
//      Object IllIIIllIl = lIIlIllIIl.invoke(null);
//
//      Object lIlIllIIll = IllIIIllIl.getClass().getMethod("getSession").invoke(IllIIIllIl);
//      Object lIIlIlIllI = lIlIllIIll.getClass().getMethod("getProfile").invoke(lIlIllIIll);
//      String lIIlIlllIl = (String) lIIlIlIllI.getClass().getMethod("getId").invoke(lIIlIlIllI);
//
//      boolean IllIIlllII = IlIIlIllIl != null && IlIIlIllIl.equals(lIIlIlllIl.toString());
//
//      Method IlIlIIlIll = lIIlIlIlIl.getClass().getMethod("getBugType");
//      String lIIlIlIIlI = (String) IlIlIIlIll.invoke(lIIlIlIlIl);
//
//      return IllIIlllII && lIIlIlIIlI != null && lIIlIlIIlI.equals(IlIIlIlIlI);
//    } catch (Exception e) {
//      return false;
//    }
//  }
//
//  private static BufferedReader IlIlIllIIl(String lIIlIlllIl) throws Exception {
//    String IlIIlIlllI = lIIlIIllIl + lIIlIlllIl;
//    HttpURLConnection IllIIllIIl = (HttpURLConnection) new URL(IlIIlIlllI).openConnection();
//    IllIIllIIl.setRequestMethod(decryptString(new byte[]{-126, -123, -120, -121, -116}));
//
//    String lIIlIIlIlI = decryptString(new byte[]{-122, -105, -108, -109, -120, -63, -121, -105, -109, -108});
//    String IlIlIIlIlI = decryptString(new byte[]{-121, -108, -108, -105, -122, -109, -126, -63, -113, -108, -121, -108, -117});
//    IllIIllIIl.setRequestProperty(lIIlIIlIlI, IlIlIIlIlI);
//
//    int lIIlIIllII = IllIIllIIl.getResponseCode();
//    return new BufferedReader(new InputStreamReader(
//        lIIlIIllII >= 200 && lIIlIIllII < 300 ?
//            IllIIllIIl.getInputStream() :
//            IllIIllIIl.getErrorStream()));
//  }
//
//  private static String decryptString(byte[] IlIlIIIllI) {
//    byte[] lIlIIllIIl = new byte[IlIlIIIllI.length];
//    for (int IllIIlllII = 0; IllIIlllII < IlIlIIIllI.length; IllIIlllII++) {
//      lIlIIllIIl[IllIIlllII] = (byte) (IlIlIIIllI[IllIIlllII] + 64);
//    }
//    return new String(lIlIIllIIl);
//  }
//}