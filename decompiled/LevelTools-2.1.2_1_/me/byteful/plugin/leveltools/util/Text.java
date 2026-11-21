package me.byteful.plugin.leveltools.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class Text {
   private static final Set<Character> COLOR_CODES = new HashSet<>();
   private static final ThreadLocal<DecimalFormat> FORMAT;
   private static final char[] SUFFIXES;

   @NotNull
   public static String decolorize(@NotNull String string) {
      return colorize(string).replace("§", "&");
   }

   @NotNull
   public static String colorize(String input) {
      StringBuilder builder = new StringBuilder();
      int len = input.length();

      for (int i = 0; i < len; i++) {
         char c = input.charAt(i);
         if (i + 1 < len) {
            char n = input.charAt(i + 1);
            if (c == '\\' && (n == '&' || n == '\\')) {
               builder.append(n);
               i++;
               continue;
            }

            if (c == '&') {
               if (COLOR_CODES.contains(Character.toLowerCase(n))) {
                  builder.append('§').append(Character.toLowerCase(n));
                  i++;
                  continue;
               }

               if (n == '#' && i + 7 < len) {
                  String hexCode = input.substring(i + 2, i + 8);
                  if (hexCode.chars().allMatch(Text::isHexChar)) {
                     builder.append('§').append('x');

                     for (char hc : hexCode.toCharArray()) {
                        builder.append('§').append(Character.toLowerCase(hc));
                     }

                     i += 7;
                     continue;
                  }
               }
            }
         }

         builder.append(c);
      }

      return builder.toString();
   }

   private static boolean isHexChar(int c) {
      return c >= 48 && c <= 57 || c >= 65 && c <= 70 || c >= 97 && c <= 102;
   }

   public static String formatMoney(double money) {
      int i;
      for (i = -1; money >= 1000.0; i++) {
         money /= 1000.0;
      }

      return i >= 0 ? truncateDouble(money) + SUFFIXES[i] : truncateDouble(money);
   }

   public static String truncateDouble(double input) {
      return FORMAT.get().format(input);
   }

   public static String[] substringsBetween(String str, String open, String close) {
      int strLen = str.length();
      int closeLen = close.length();
      int openLen = open.length();
      List<String> list = new ArrayList<>();
      int pos = 0;

      while (pos < strLen - closeLen) {
         int start = str.indexOf(open, pos);
         if (start < 0) {
            break;
         }

         start += openLen;
         int end = str.indexOf(close, start);
         if (end < 0) {
            break;
         }

         list.add(str.substring(start, end));
         pos = end + closeLen;
      }

      return list.isEmpty() ? null : list.toArray(new String[0]);
   }

   static {
      for (char c : "0123456789abcdefklmnor".toCharArray()) {
         COLOR_CODES.add(c);
      }

      FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("0.00"));
      SUFFIXES = new char[]{'K', 'M', 'B', 'T', 'Q'};
   }
}
