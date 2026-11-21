package com.skyblockexp.ezrtp.util;

public final class TimeUtil {
   private TimeUtil() {
   }

   public static String formatCooldownTime(long seconds) {
      if (seconds < 60L) {
         return seconds + "s";
      } else if (seconds < 3600L) {
         return seconds / 60L + "m " + seconds % 60L + "s";
      } else {
         long hours = seconds / 3600L;
         long minutes = seconds % 3600L / 60L;
         return hours + "h " + minutes + "m";
      }
   }
}
