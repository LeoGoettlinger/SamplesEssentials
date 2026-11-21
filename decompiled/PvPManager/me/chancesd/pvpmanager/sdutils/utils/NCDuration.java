package me.chancesd.pvpmanager.sdutils.utils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NCDuration {
   private static final Pattern pattern = Pattern.compile("(\\d+)([smhd])");
   private static final int HOURS_PER_DAY = 24;
   private static final int MINUTES_PER_HOUR = 60;
   private static final int SECONDS_PER_MINUTE = 60;
   private static final int SECONDS_PER_HOUR = 3600;
   private static final int SECONDS_PER_DAY = 86400;
   private final Duration duration;

   private NCDuration(Duration duration) {
      this.duration = duration;
   }

   public static NCDuration between(Temporal startInclusive, Temporal endExclusive) {
      return new NCDuration(Duration.between(startInclusive, endExclusive));
   }

   public static NCDuration ofMillis(long millis) {
      return new NCDuration(Duration.ofMillis(millis));
   }

   public static NCDuration parseDuration(String durationString) {
      if (durationString != null && !durationString.trim().isEmpty()) {
         Matcher matcher = pattern.matcher(durationString.toLowerCase());
         long totalMillis = 0L;
         boolean foundMatch = false;

         while (matcher.find()) {
            foundMatch = true;
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
               case "s":
                  totalMillis += value * 1000L;
                  break;
               case "m":
                  totalMillis += value * 60L * 1000L;
                  break;
               case "h":
                  totalMillis += value * 60L * 60L * 1000L;
                  break;
               case "d":
                  totalMillis += value * 24L * 60L * 60L * 1000L;
                  break;
               default:
                  throw new IllegalArgumentException("Invalid time unit: " + unit);
            }
         }

         if (!foundMatch) {
            throw new IllegalArgumentException(
               "Invalid duration format: " + durationString + ". Expected format like '30s', '5m', '2h', '1d' or combinations like '1h30m'"
            );
         } else {
            return ofMillis(totalMillis);
         }
      } else {
         throw new IllegalArgumentException("Duration string cannot be null or empty");
      }
   }

   public int toDaysPart() {
      return (int)(this.duration.getSeconds() / 86400L);
   }

   public int toHoursPart() {
      return (int)(this.duration.toHours() % 24L);
   }

   public int toMinutesPart() {
      return (int)(this.duration.toMinutes() % 60L);
   }

   public int toSecondsPart() {
      return (int)(this.duration.getSeconds() % 60L);
   }

   public int toMillisPart() {
      return this.duration.getNano() / 1000000;
   }

   public boolean isZero() {
      return this.duration.isZero();
   }

   public boolean isNotZero() {
      return !this.duration.isZero();
   }

   public NCDuration plusMillis(long milis) {
      return new NCDuration(this.duration.plusMillis(milis));
   }

   public int get(ChronoUnit unit) {
      switch (unit) {
         case DAYS:
            return this.toDaysPart();
         case HOURS:
            return this.toHoursPart();
         case MILLIS:
            return this.toMillisPart();
         case MINUTES:
            return this.toMinutesPart();
         case SECONDS:
            return this.toSecondsPart();
         default:
            return 0;
      }
   }

   public Duration getDuration() {
      return this.duration;
   }
}
