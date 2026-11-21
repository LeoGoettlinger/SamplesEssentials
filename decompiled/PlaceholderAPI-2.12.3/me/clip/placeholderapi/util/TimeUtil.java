package me.clip.placeholderapi.util;

import java.time.Duration;
import java.util.StringJoiner;

public class TimeUtil {
   public static String getRemaining(int seconds, TimeFormat type) {
      return getRemaining((long)seconds, type);
   }

   public static String getRemaining(long seconds, TimeFormat type) {
      switch (type) {
         case SECONDS:
            return String.valueOf(seconds % 60L);
         case MINUTES:
            return String.valueOf(seconds / 60L % 60L);
         case HOURS:
            return String.valueOf(seconds / 3600L % 24L);
         case DAYS:
            return String.valueOf(seconds / 86400L);
         default:
            return String.valueOf(seconds);
      }
   }

   public static String getTime(Duration duration) {
      return getTime(duration.getSeconds());
   }

   public static String getTime(int seconds) {
      return getTime((long)seconds);
   }

   public static String getTime(long seconds) {
      StringJoiner joiner = new StringJoiner(" ");
      long minutes = seconds / 60L;
      long hours = minutes / 60L;
      long days = hours / 24L;
      seconds %= 60L;
      minutes %= 60L;
      hours %= 24L;
      if (days > 0L) {
         joiner.add(days + "d");
      }

      if (hours > 0L) {
         joiner.add(hours + "h");
      }

      if (minutes > 0L) {
         joiner.add(minutes + "m");
      }

      if (seconds > 0L) {
         joiner.add(seconds + "s");
      }

      return joiner.toString();
   }
}
