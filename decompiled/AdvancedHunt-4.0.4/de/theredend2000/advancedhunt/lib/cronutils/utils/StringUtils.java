package de.theredend2000.advancedhunt.lib.cronutils.utils;

import java.util.StringJoiner;

public class StringUtils {
   public static final String EMPTY = "";

   private StringUtils() {
   }

   public static boolean isEmpty(CharSequence cs) {
      return cs == null || cs.length() == 0;
   }

   public static boolean containsAny(CharSequence cs, char... searchChars) {
      if (!isEmpty(cs) && searchChars != null && searchChars.length != 0) {
         int csLength = cs.length();
         int searchLength = searchChars.length;
         int csLast = csLength - 1;
         int searchLast = searchLength - 1;

         for (int i = 0; i < csLength; i++) {
            char ch = cs.charAt(i);

            for (int j = 0; j < searchLength; j++) {
               if (searchChars[j] == ch) {
                  if (!Character.isHighSurrogate(ch)) {
                     return true;
                  }

                  if (j == searchLast) {
                     return true;
                  }

                  if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isNumeric(CharSequence cs) {
      if (isEmpty(cs)) {
         return false;
      } else {
         int sz = cs.length();

         for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public static String join(Object[] parts, String separator) {
      if (parts == null) {
         return null;
      } else {
         StringJoiner joiner = new StringJoiner(separator == null ? "" : separator);

         for (Object part : parts) {
            joiner.add(String.valueOf(part));
         }

         return joiner.toString();
      }
   }
}
