package me.chancesd.pvpmanager.sdutils.config;

public class KeyUtils {
   public static boolean isSubKeyOf(String parentKey, String subKey, char separator) {
      return parentKey.isEmpty() ? false : subKey.startsWith(parentKey) && subKey.substring(parentKey.length()).startsWith(String.valueOf(separator));
   }

   public static String getIndents(String key, char separator) {
      String[] splitKey = key.split("[" + separator + "]");
      StringBuilder builder = new StringBuilder();

      for (int i = 1; i < splitKey.length; i++) {
         builder.append("  ");
      }

      return builder.toString();
   }
}
