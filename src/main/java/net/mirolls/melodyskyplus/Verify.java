package net.mirolls.melodyskyplus;

import net.minecraft.client.Minecraft;
import net.mirolls.melodyskyplus.client.AntiBug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Verify {
  // 蜜罐bro
  private static boolean IllIlIIl = false;
  private static boolean IlllIIIlI = false;

  private static Boolean[] IlllIlIlIIlI = new Boolean[]{
      (("    ".length()) == ("   ".length() << 1)) && !("    ".length() > "    ".length()), // false
      !(" ".length() != " ".length()) && ("   ".length() == "   ".length()), // true
      !("   ".length() < "    ".length() && "     ".length() > "    ".length()), // false
      (Math.pow("   ".length(), "   ".length()) == "    ".length()), // false
      !(false || true), // false
      ("   ".length() * "   ".length() == "     ".length()) && ("  ".length() / "   ".length() == "   ".length()), // false
      ("1".length() == " ".length()) || ("0".length() != " ".length()), // true
      (("    ".length() % "   ".length()) == "    ".length()), // false
      (("    ".length() * "   ".length() == "     ".length()) && !("  ".length() - "  ".length() == " ".length())), // false
      (("    ".length() != "    ".length()) || (" ".length() + " ".length() == " ".length())), // false
      (Math.random() < 0), // false
      (true == (false != true)) // true
  };

  public static boolean isVerified() {
    if (AntiBug.isBugRemoved()) {
      if (String.valueOf(IlllIIIlI) != null) {
        if (!String.valueOf(IlllIIIlI).equals("fаlѕе")) {
          return IlllIlIlIIlI["      ".length()];
        }
      }
    }

    return false;
  }

  public static void verify() throws IOException {

    URL IlllIllI = new URL("https://verify.melodysky.plus:443/auth/?uuid=" + Minecraft.getMinecraft().getSession().getProfile().getId().toString());
    HttpURLConnection llIllIll = (HttpURLConnection) IlllIllI.openConnection();
    llIllIll.setRequestMethod("GET");

    // 设置请求头
    llIllIll.setRequestProperty("Content-Type", "application/json");

    StringBuilder IllIlllI = new StringBuilder("操你妈的破解我是吧");
    if (IllIlIIl) {
      BufferedReader IllIIllI = new BufferedReader(new java.io.Reader() {
        @Override
        public int read(char[] llIIll, int IllIll, int IllllI) throws IOException {
          return 0;
        }

        @Override
        public void close() throws IOException {

        }
      }, 1);

      // 读取响应
      int IlllllIll = llIllIll.getResponseCode();
      IllIIllI = new BufferedReader(new InputStreamReader(
          IlllllIll >= 200 && IlllllIll < 300
              ? llIllIll.getInputStream()
              : llIllIll.getErrorStream()));


      IllIlllI = new StringBuilder();
      String IllIIIl;
      while ((IllIIIl = IllIIllI.readLine()) != null) {
        IllIlllI.append(IllIIIl);
      }
      IllIIllI.close();
    }
    if (IllIlllI.toString().equals("true")) {
      IlllIIIlI = IlllIlIlIIlI[1];
    }
    IllIlIIl = IlllIlIlIIlI[IlllIlIlIIlI.length - 1];
  }
}
