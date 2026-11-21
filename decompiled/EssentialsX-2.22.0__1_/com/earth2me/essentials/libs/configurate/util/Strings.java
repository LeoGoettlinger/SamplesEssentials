package com.earth2me.essentials.libs.configurate.util;

import java.util.Objects;

public final class Strings {
   private Strings() {
   }

   public static String repeat(final String content, final int times) {
      Objects.requireNonNull(content, "content");
      switch (times) {
         case 0:
            return "";
         case 1:
            return content;
         default:
            StringBuilder ret = new StringBuilder(content.length() * times);

            for (int i = 0; i < times; i++) {
               ret.append(content);
            }

            return ret.toString();
      }
   }

   public static boolean isBlank(final String test) {
      int i = 0;

      for (int len = test.length(); i < len; i++) {
         if (!Character.isWhitespace(test.charAt(i))) {
            return false;
         }
      }

      return true;
   }
}
