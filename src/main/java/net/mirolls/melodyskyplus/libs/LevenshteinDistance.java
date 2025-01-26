package net.mirolls.melodyskyplus.libs;

public class LevenshteinDistance {
  // 计算Levenshtein距离
  public static int computeDistance(String str1, String str2) {
    int len1 = str1.length();
    int len2 = str2.length();
    int[][] dp = new int[len1 + 1][len2 + 1];

    for (int i = 0; i <= len1; i++) dp[i][0] = i;
    for (int j = 0; j <= len2; j++) dp[0][j] = j;

    for (int i = 1; i <= len1; i++) {
      for (int j = 1; j <= len2; j++) {
        if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
              Math.min(dp[i - 1][j], dp[i][j - 1]));
        }
      }
    }
    return dp[len1][len2];
  }

  // 检查是否满足模糊匹配的阈值
  public static boolean isFuzzyMatch(String target, String input, int threshold) {
    return computeDistance(target, input) <= threshold;
  }
}