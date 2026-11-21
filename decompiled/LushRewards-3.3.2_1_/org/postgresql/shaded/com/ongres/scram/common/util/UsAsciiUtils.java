package org.postgresql.shaded.com.ongres.scram.common.util;

public class UsAsciiUtils {
   public static String toPrintable(String value) throws IllegalArgumentException {
      Preconditions.checkNotNull(value, "value");
      char[] printable = new char[value.length()];
      int i = 0;

      for (char chr : value.toCharArray()) {
         if (chr < 0 || chr >= 127) {
            throw new IllegalArgumentException("value contains character '" + chr + "' which is non US-ASCII");
         }

         if (chr > ' ') {
            printable[i++] = chr;
         }
      }

      return i == value.length() ? value : new String(printable, 0, i);
   }
}
