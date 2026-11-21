package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Message;
import ch.njol.util.Math2;
import ch.njol.yggdrasil.YggdrasilSerializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.util.Cyclical;

public class Time implements YggdrasilSerializable, Cyclical<Integer> {
   private static final int TICKS_PER_HOUR = 1000;
   private static final int TICKS_PER_DAY = 24000;
   private static final double TICKS_PER_MINUTE = 16.666666666666668;
   private static final int HOUR_ZERO = 6000;
   private final int time;
   private static final Pattern DAY_TIME_PATTERN = Pattern.compile("(\\d?\\d)(:(\\d\\d))? ?(am|pm)", 2);
   private static final Pattern TIME_PATTERN = Pattern.compile("\\d?\\d:\\d\\d", 2);
   private static final Message m_error_24_hours = new Message("time.errors.24 hours");
   private static final Message m_error_12_hours = new Message("time.errors.12 hours");
   private static final Message m_error_60_minutes = new Message("time.errors.60 minutes");

   public Time() {
      this(0);
   }

   public Time(int time) {
      this.time = Math2.mod(time, 24000);
   }

   public int getTicks() {
      return this.time;
   }

   public int getTime() {
      return (this.time + 6000) % 24000;
   }

   public int getHour() {
      int thisTime = this.time;
      if (this.time < 0) {
         thisTime = Math.abs(this.time);
      }

      int hour = (thisTime + 6000) / 1000;
      if (hour >= 24) {
         hour -= 24;
      }

      return hour;
   }

   public int getMinute() {
      return (int)Math2.round((this.time + 6000) % 1000 / 16.666666666666668);
   }

   @Override
   public String toString() {
      return toString(this.time);
   }

   public static String toString(int ticks) {
      assert 0 <= ticks && ticks < 24000;

      int t = (ticks + 6000) % 24000;
      int hours = t / 1000;
      int minutes = (int)Math.round(t % 1000 / 16.666666666666668);
      if (minutes >= 60) {
         hours = (hours + 1) % 24;
         minutes -= 60;
      }

      return hours + ":" + (minutes < 10 ? "0" : "") + minutes;
   }

   @Nullable
   public static Time parse(String s) {
      if (TIME_PATTERN.matcher(s).matches()) {
         int hours = Utils.parseInt(s.split(":")[0]);
         if (hours == 24) {
            hours = 0;
         } else if (hours > 24) {
            Skript.error(m_error_24_hours + "");
            return null;
         }

         int minutes = Utils.parseInt(s.split(":")[1]);
         if (minutes >= 60) {
            Skript.error(m_error_60_minutes + "");
            return null;
         } else {
            return new Time((int)Math.round(hours * 1000 - 6000 + minutes * 16.666666666666668));
         }
      } else {
         Matcher m = DAY_TIME_PATTERN.matcher(s);
         if (m.matches()) {
            int hoursx = Utils.parseInt(m.group(1));
            if (hoursx == 12) {
               hoursx = 0;
            } else if (hoursx > 12) {
               Skript.error(m_error_12_hours + "");
               return null;
            }

            int minutes = 0;
            if (m.group(3) != null) {
               minutes = Utils.parseInt(m.group(3));
            }

            if (minutes >= 60) {
               Skript.error(m_error_60_minutes + "");
               return null;
            } else {
               if (m.group(4).equalsIgnoreCase("pm")) {
                  hoursx += 12;
               }

               return new Time((int)Math.round(hoursx * 1000 - 6000 + minutes * 16.666666666666668));
            }
         } else {
            return null;
         }
      }
   }

   @Override
   public int hashCode() {
      return this.time;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return obj instanceof Time other ? this.time == other.time : false;
      }
   }

   public Integer getMaximum() {
      return 24000;
   }

   public Integer getMinimum() {
      return 0;
   }
}
