package su.nightexpress.nightcore.util;

import org.jspecify.annotations.NonNull;

public class LegacyColors {
   private static final char COLOR_CHAR = '§';
   public static final char ALT_COLOR_CHAR = '&';
   private static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

   @NonNull
   public static String plainColors(@NonNull String string) {
      string = plainHEXColors(string);
      return plainDefaultColors(string);
   }

   @NonNull
   public static String plainDefaultColors(@NonNull String string) {
      char[] b = string.toCharArray();

      for (int i = 0; i < b.length - 1; i++) {
         if (b[i] == 167 && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
            b[i] = '&';
            b[i + 1] = Character.toLowerCase(b[i + 1]);
         }
      }

      return new String(b);
   }

   @NonNull
   public static String plainHEXColors(@NonNull String str) {
      StringBuilder builder = new StringBuilder(str);

      int index;
      while ((index = builder.toString().indexOf("§x")) >= 0) {
         int count = 0;
         builder.replace(index, index + 2, "#");

         for (int point = index + 1; count < 6; point++) {
            builder.deleteCharAt(point);
            count++;
         }
      }

      return builder.toString();
   }
}
