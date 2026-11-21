package com.viaversion.viaversion.util;

public final class StringUtil {
   public static String forLogging(Object o) {
      return forLogging(String.valueOf(o));
   }

   public static String forLogging(String s) {
      StringBuilder builder = new StringBuilder(s.length());
      int i = 0;

      for (int len = s.length(); i < len; i++) {
         char c = s.charAt(i);
         if (c == '\t' || c == '\n' || c == '\r') {
            builder.append(' ');
         } else if (!Character.isISOControl(c)) {
            builder.append(c);
         }
      }

      return builder.toString();
   }
}
