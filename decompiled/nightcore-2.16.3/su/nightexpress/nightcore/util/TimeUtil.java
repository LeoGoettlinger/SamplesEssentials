package su.nightexpress.nightcore.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;

public class TimeUtil {
   private static TimeZone timeZone;

   public static void setTimeZone(@NonNull String name) {
      timeZone = TimeZone.getTimeZone(name);
   }

   @NonNull
   public static TimeZone getTimeZone() {
      return timeZone;
   }

   @NonNull
   public static ZoneId getZoneId() {
      return timeZone.toZoneId();
   }

   @Deprecated
   @NonNull
   public static String formatTime(long time) {
      return TimeFormats.toLiteral(time);
   }

   public static boolean isPassed(long timestamp) {
      return timestamp >= 0L && System.currentTimeMillis() > timestamp;
   }

   public static long createFutureTimestamp(double seconds) {
      return seconds < 0.0 ? -1L : createTimestamp(Math.abs(seconds));
   }

   public static long createFutureTimestamp(long duration, @NonNull TimeUnit timeUnit) {
      if (duration < 0L) {
         return -1L;
      } else {
         long millis = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
         return System.currentTimeMillis() + millis;
      }
   }

   public static long createPastTimestamp(double seconds) {
      return createTimestamp(-Math.abs(seconds));
   }

   public static long createTimestamp(double seconds) {
      return System.currentTimeMillis() + (long)(seconds * 1000.0);
   }

   public static long secondsToTicks(int seconds) {
      return seconds * 20L;
   }

   public static double ticksToSeconds(long ticks) {
      return ticks / 20.0;
   }

   @Deprecated
   @NonNull
   public static String formatDuration(long from, long to) {
      long time = to - from;
      return TimeFormats.toLiteral(time);
   }

   @Deprecated
   @NonNull
   public static String formatDuration(long until) {
      return TimeFormats.formatDuration(until, TimeFormatType.LITERAL);
   }

   @NonNull
   public static LocalTime getLocalTimeOf(long ms) {
      long hours = TimeUnit.MILLISECONDS.toHours(ms) % 24L;
      long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60L;
      long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60L;
      return LocalTime.of((int)hours, (int)minutes, (int)seconds);
   }

   @NonNull
   public static LocalDateTime getCurrentDateTime() {
      return LocalDateTime.now(getZoneId());
   }

   @NonNull
   public static LocalDate getCurrentDate() {
      return LocalDate.now(getZoneId());
   }

   @NonNull
   public static LocalTime getCurrentTime() {
      return LocalTime.now(getZoneId());
   }

   @NonNull
   public static LocalDateTime getLocalDateTimeOf(long ms) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), getZoneId());
   }

   public static long toEpochMillis(@NonNull LocalDateTime dateTime) {
      Instant instant = dateTime.atZone(getZoneId()).toInstant();
      return instant.toEpochMilli();
   }
}
