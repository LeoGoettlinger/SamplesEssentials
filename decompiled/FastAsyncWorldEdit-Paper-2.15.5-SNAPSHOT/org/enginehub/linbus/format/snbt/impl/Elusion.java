package org.enginehub.linbus.format.snbt.impl;

public final class Elusion {
   public static boolean isSafeCharacter(char c) {
      return 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z' || '0' <= c && c <= '9' || c == '_' || c == '.' || c == '+' || c == '-';
   }

   public static CharSequence escapeIfNeeded(String s) {
      boolean totallySafe = true;
      int singleCharCount = 0;
      int doubleCharCount = 0;
      int backslashCount = 0;

      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (!totallySafe || !isSafeCharacter(c)) {
            totallySafe = false;
            switch (c) {
               case '"':
                  doubleCharCount++;
                  break;
               case '\'':
                  singleCharCount++;
                  break;
               case '\\':
                  backslashCount++;
            }
         }
      }

      if (totallySafe) {
         return s;
      } else {
         int leastCharCount = Math.min(singleCharCount, doubleCharCount);
         StringBuilder builder = new StringBuilder(s.length() + backslashCount + leastCharCount + 2);
         char quoteChar = (char)(leastCharCount == singleCharCount ? 39 : 34);
         builder.append(quoteChar);

         for (int ix = 0; ix < s.length(); ix++) {
            char c = s.charAt(ix);
            if (c == '\\' || c == quoteChar) {
               builder.append('\\');
            }

            builder.append(c);
         }

         builder.append(quoteChar);
         return builder;
      }
   }

   private Elusion() {
   }
}
