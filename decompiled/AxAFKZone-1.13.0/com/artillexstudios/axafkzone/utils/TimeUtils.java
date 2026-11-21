package com.artillexstudios.axafkzone.utils;

import com.artillexstudios.axafkzone.AxAFKZone;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;

public class TimeUtils {
   @NotNull
   public static String fancyTime(long time) {
      if (time < 0L) {
         return "---";
      } else {
         Duration remainingTime = Duration.ofMillis(time);
         long total = remainingTime.getSeconds();
         long days = total / 86400L;
         long hours = total % 86400L / 3600L;
         long minutes = total % 3600L / 60L;
         long seconds = total % 60L;
         if (AxAFKZone.CONFIG.getInt("timer-format", 1) == 1) {
            if (days > 0L) {
               return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
            } else {
               return hours > 0L ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
            }
         } else if (AxAFKZone.CONFIG.getInt("timer-format", 1) == 2) {
            if (days > 0L) {
               return days + AxAFKZone.LANG.getString("time.day", "d");
            } else if (hours > 0L) {
               return hours + AxAFKZone.LANG.getString("time.hour", "h");
            } else {
               return minutes > 0L ? minutes + AxAFKZone.LANG.getString("time.minute", "m") : seconds + AxAFKZone.LANG.getString("time.second", "s");
            }
         } else if (days > 0L) {
            return String.format(
               "%02d"
                  + AxAFKZone.LANG.getString("time.day", "d")
                  + " %02d"
                  + AxAFKZone.LANG.getString("time.hour", "h")
                  + " %02d"
                  + AxAFKZone.LANG.getString("time.minute", "m")
                  + " %02d"
                  + AxAFKZone.LANG.getString("time.second", "s"),
               days,
               hours,
               minutes,
               seconds
            );
         } else {
            return hours > 0L
               ? String.format(
                  "%02d"
                     + AxAFKZone.LANG.getString("time.hour", "h")
                     + " %02d"
                     + AxAFKZone.LANG.getString("time.minute", "m")
                     + " %02d"
                     + AxAFKZone.LANG.getString("time.second", "s"),
                  hours,
                  minutes,
                  seconds
               )
               : String.format("%02d" + AxAFKZone.LANG.getString("time.minute", "m") + " %02d" + AxAFKZone.LANG.getString("time.second", "s"), minutes, seconds);
         }
      }
   }
}
