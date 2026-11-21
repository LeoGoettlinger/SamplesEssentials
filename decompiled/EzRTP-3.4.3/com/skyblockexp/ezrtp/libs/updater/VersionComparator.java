package com.skyblockexp.ezrtp.libs.updater;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class VersionComparator {
   private static final Pattern SEMVER_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-([0-9A-Za-z.-]+))?(?:\\+([0-9A-Za-z.-]+))?$");

   private VersionComparator() {
   }

   static boolean isNewer(String currentVersion, String latestVersion) {
      String normalizedCurrent = normalize(currentVersion);
      String normalizedLatest = normalize(latestVersion);
      Matcher currentMatcher = SEMVER_PATTERN.matcher(normalizedCurrent);
      Matcher latestMatcher = SEMVER_PATTERN.matcher(normalizedLatest);
      return currentMatcher.matches() && latestMatcher.matches()
         ? compareSemver(currentMatcher, latestMatcher) < 0
         : fallbackCompare(normalizedCurrent, normalizedLatest) < 0;
   }

   static String normalize(String value) {
      if (value == null) {
         return "";
      } else {
         String trimmed = value.trim();
         return trimmed.toLowerCase(Locale.ROOT).startsWith("v") ? trimmed.substring(1) : trimmed;
      }
   }

   private static int compareSemver(Matcher current, Matcher latest) {
      for (int i = 1; i <= 3; i++) {
         int c = Integer.parseInt(current.group(i));
         int l = Integer.parseInt(latest.group(i));
         if (c != l) {
            return Integer.compare(c, l);
         }
      }

      String cPre = current.group(4);
      String lPre = latest.group(4);
      if (cPre == null && lPre == null) {
         return 0;
      } else if (cPre == null) {
         return 1;
      } else {
         return lPre == null ? -1 : comparePreRelease(cPre, lPre);
      }
   }

   private static int comparePreRelease(String a, String b) {
      List<String> aParts = split(a);
      List<String> bParts = split(b);
      int max = Math.max(aParts.size(), bParts.size());

      for (int i = 0; i < max; i++) {
         if (i >= aParts.size()) {
            return -1;
         }

         if (i >= bParts.size()) {
            return 1;
         }

         String left = aParts.get(i);
         String right = bParts.get(i);
         boolean leftNum = isNumeric(left);
         boolean rightNum = isNumeric(right);
         if (leftNum && rightNum) {
            int cmp = Integer.compare(Integer.parseInt(left), Integer.parseInt(right));
            if (cmp != 0) {
               return cmp;
            }
         } else {
            if (leftNum) {
               return -1;
            }

            if (rightNum) {
               return 1;
            }

            int cmp = left.compareTo(right);
            if (cmp != 0) {
               return cmp;
            }
         }
      }

      return 0;
   }

   private static List<String> split(String value) {
      String[] parts = value.split("\\.");
      List<String> out = new ArrayList<>(parts.length);

      for (String part : parts) {
         out.add(part);
      }

      return out;
   }

   private static boolean isNumeric(String value) {
      for (int i = 0; i < value.length(); i++) {
         if (!Character.isDigit(value.charAt(i))) {
            return false;
         }
      }

      return !value.isEmpty();
   }

   private static int fallbackCompare(String current, String latest) {
      List<Object> cTokens = tokenize(current);
      List<Object> lTokens = tokenize(latest);
      int max = Math.max(cTokens.size(), lTokens.size());

      for (int i = 0; i < max; i++) {
         if (i >= cTokens.size()) {
            return -1;
         }

         if (i >= lTokens.size()) {
            return 1;
         }

         Object c = cTokens.get(i);
         Object l = lTokens.get(i);
         int cmp;
         if (c instanceof Integer && l instanceof Integer) {
            cmp = Integer.compare((Integer)c, (Integer)l);
         } else {
            cmp = c.toString().compareToIgnoreCase(l.toString());
         }

         if (cmp != 0) {
            return cmp;
         }
      }

      return 0;
   }

   private static List<Object> tokenize(String input) {
      List<Object> tokens = new ArrayList<>();
      Matcher m = Pattern.compile("(\\d+|[A-Za-z]+)").matcher(input);

      while (m.find()) {
         String token = m.group(1);
         if (isNumeric(token)) {
            tokens.add(Integer.parseInt(token));
         } else {
            tokens.add(token.toLowerCase(Locale.ROOT));
         }
      }

      if (tokens.isEmpty()) {
         tokens.add(input.toLowerCase(Locale.ROOT));
      }

      return tokens;
   }
}
